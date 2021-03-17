package semp.cfg;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import lombok.Getter;
import semp.cfg.model.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.StringWriter;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class SempClient {
    private static final String CONFIG_BASE_PATH = "/SEMP/v2/config";
    public static final int HTTP_OK = 200;

    @Getter private final String baseUrl;
    @Getter private final String adminUser;
    @Getter private final String adminPwd;
    @Getter private String opaquePassword;
    private final HttpClient httpClient;

    public SempClient(String adminUrl, String adminUser, String adminPwd, boolean insecure, Path cacert) {
        this.baseUrl = adminUrl+ CONFIG_BASE_PATH;
        this.adminUser = adminUser;
        this.adminPwd = adminPwd;

        var b =  HttpClient.newBuilder();
        if (insecure) {
            Optional.ofNullable(getInscureSSLContext()).ifPresent(b::sslContext);
        } else if (Objects.nonNull(cacert)) {
            Optional.ofNullable(getSSLContextFrom(cacert)).ifPresent(b::sslContext);
        }
        this.httpClient = b
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .authenticator(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(adminUser, adminPwd.toCharArray());
                    }
                })
                .build();
    }

    private SSLContext getInscureSSLContext() {
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        try {
            var sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            return sc;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
        return null;
    }

    private SSLContext getSSLContextFrom(Path crtFile) {
        try {
            var certificate = CertificateFactory.getInstance("X.509").generateCertificate(Files.newInputStream(crtFile));
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("server", certificate);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            return sslContext;
        } catch (CertificateException | IOException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setOpaquePassword(String opaquePassword) {
        if (Optional.ofNullable(opaquePassword).map(String::isEmpty).orElse(true)) {
            return;
        }
        if (SempSpec.getSempVersion().compareTo(new SempVersion("2.17")) < 0) {
            Utils.errPrintlnAndExit("The SEMPv2 version of this broker is %s, Opaque Password is only supported since version 9.6(sempVersion 2.17)",
                    SempSpec.getSempVersion());
        }
        if (!baseUrl.substring(0, 5).equalsIgnoreCase("https")) {
            Utils.errPrintlnAndExit("Opaque Password is only supported over HTTPS!");
        }
        if (opaquePassword.length() < 8 || opaquePassword.length() > 128) {
            Utils.errPrintlnAndExit("Opaque Password must be between 8 and 128 characters inclusive!");
        }
        this.opaquePassword = opaquePassword;
    }

    public String uriAddOpaquePassword(String uri) {
        if (Optional.ofNullable(opaquePassword).map(String::isEmpty).orElse(true)) {
            return uri;
        }
        var q = (uri.contains("?") ? "&" : "?") + SempSpec.OPAQUE_PASSWORD + "=" + opaquePassword;
        return uri + q;
    }

    public String getBrokerSpec() {
        return sendWithResourcePath(HTTPMethod.GET.name(), "/spec", null);
    }

    /**
     * If the collection is large, this method will follow the "nextPageUri" field
     * in the response to continually fetch all results.
     * @param absUri MUST be a collection path
     * @return a SempRespone with all data and links from the absUri
     */
    public SempResponse getCollectionWithAbsoluteUri(String absUri){
        List<SempResponse> responseList = new LinkedList<>();
        Optional<String> nextPageUri = Optional.of(absUri);
        while (nextPageUri.isPresent()){
            SempResponse resp = SempResponse.ofString(sendWithAbsoluteURI("GET", uriAddOpaquePassword(nextPageUri.get()), null));
            responseList.add(resp);
            nextPageUri = resp.getNextPageUri();
        }
        // Combine all paging results into one SempResponse
        var result = responseList.stream().reduce((r1, r2)->{
            r1.getData().addAll(r2.getData());
            r1.getLinks().addAll(r2.getLinks());
            r1.setMeta(r2.getMeta());
            return r1;
        });
        return result.orElse(null);
    }

    public String buildAbsoluteUri(String resourcePath){
        return baseUrl+resourcePath;
    }

    /**
     * Send a SEMPv2 request, and return only the meta part of the response.
     */
    public SempMeta sendAndGetMeta(String method, String resourcePath, String payload) {
        String uri = uriAddOpaquePassword(resourcePath);
        return SempMeta.ofString(sendWithResourcePath(method, uri, payload));
    }

    public String sendWithResourcePath(String method, String resourcePath, String payload) {
        return sendWithAbsoluteURI(method, buildAbsoluteUri(resourcePath), payload);
    }

    private String sendWithAbsoluteURI(String method, String absUri, String payload) {
        var bp = Objects.isNull(payload) || payload.isEmpty() ?
                BodyPublishers.noBody() :
                BodyPublishers.ofString(payload);
        HttpRequest request = HttpRequest.newBuilder()
                .method(method.toUpperCase(), bp)
                .uri(URI.create(absUri))
                .header("content-type", "application/json")
                .build();

        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException|IOException e) {
            Utils.errPrintlnAndExit(e, "%s %s with playload:%n%s%n%s",
                    method.toUpperCase(), absUri, payload, e.toString());
        }
        var body = Optional.ofNullable(response)
                .map(HttpResponse::body);
            if (body.isEmpty() || body.get().isEmpty()) {
                Utils.errPrintlnAndExit((Exception) null,
                        "%s %s returns empty body",
                        method, absUri);
            }
        return body.orElse(null);
    }


    public static Map<String, Object> readMapFromJsonFile(Path confPath) {
        try {
            return (Map<String, Object>)Utils.objectMapper.readValue(freeMakerToString(confPath), Map.class);
        } catch (IOException e) {
            Utils.errPrintlnAndExit(e,
                    "File %s is not a valid configuration json file!",
                    confPath.toAbsolutePath());
        }
        return new HashMap<>();
    }

    private static String freeMakerToString(Path confPath) {
        try {
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
            cfg.setDirectoryForTemplateLoading(confPath.getParent().toFile());
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            cfg.setLogTemplateExceptions(false);
            cfg.setWrapUncheckedExceptions(true);
            cfg.setFallbackOnNullLoopVariable(false);

            Template temp = cfg.getTemplate(confPath.getFileName().toString());
            var stringWriter = new StringWriter();
            temp.process(null, stringWriter);
            return stringWriter.toString();
        } catch (IOException | TemplateException e) {
            Utils.errPrintlnAndExit(e, "Unable to read file %s", confPath.toAbsolutePath());
            return "";
        }
    }

    public Set<Map.Entry<String, Boolean>> checkIfObjectsExist(String resourceType, List<String> objectNames){
        var absUriList = objectNames.stream()
                .filter(n -> !n.equals("*"))
                .map(objectName -> Map.entry(objectName,
                        String.format("/%s/%s", resourceType, objectName)))
                .collect(Collectors.toList());

        Map<String, Boolean> result = new HashMap<>();
        absUriList.forEach( entry->{
            var meta = sendAndGetMeta(HTTPMethod.GET.toSEMPMethod(), entry.getValue(), null);
            if (meta.getResponseCode() == HTTP_OK) {
                result.put(entry.getKey(), true);
            } else if (meta.getError().getCode() == SEMPError.NOT_FOUND.getValue()) {
                result.put(entry.getKey(), false);
            } else {
                Utils.errPrintlnAndExit((Exception) null, "%s %s%n%s%n",
                        HTTPMethod.GET,
                        entry.getValue(),
                        meta);
            }
        });
        return result.entrySet();
    }


}
