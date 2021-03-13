package semp.cfg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import semp.cfg.model.HTTPMethod;
import semp.cfg.model.SEMPError;
import semp.cfg.model.SempMeta;
import semp.cfg.model.SempResponse;

import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class SempClient {
    private static final String CONFIG_BASE_PATH = "/SEMP/v2/config";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static final int HTTP_OK = 200;

    private final String baseUrl;
    private final HttpClient httpClient;

    public SempClient(String adminUrl, String adminUser, String adminPwd) {
        this.baseUrl = adminUrl+ CONFIG_BASE_PATH;
        httpClient = HttpClient.newBuilder()
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

    public Optional<SempResponse> getCollectionWithAbsoluteUri(String absUri){
        Optional<SempResponse> result = Optional.empty();
        try {
            List<SempResponse> responseList = new LinkedList<>();
            Optional<String> nextPageUri = Optional.of(absUri);
            while (nextPageUri.isPresent()){
                SempResponse resp = SempResponse.ofString(sendWithAbsoluteURI("GET", nextPageUri.get(), null));
                responseList.add(resp);
                nextPageUri = resp.getNextPageUri();
            }
            // Combine all paging results into one SempResponse
            result = responseList.stream().reduce((r1, r2)->{
                r1.getData().addAll(r2.getData());
                r1.getLinks().addAll(r2.getLinks());
                r1.setMeta(r2.getMeta());
                return r1;
            });
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return result;
    }

    public String buildAbsoluteUri(String resourcePath){
        return baseUrl+resourcePath;
    }

    /**
     * Send a SEMPv2 request, and return only the meta part of the response.
     */
    public SempMeta sendAndGetMeta(String method, String resourcePath, String payload) {
        return SempMeta.ofString(sendWithResourcePath(method, resourcePath, payload));
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


    public static Map<String, Object> readMapFromJsonFile(File confFile) {
        try {
            return (Map<String, Object>)objectMapper.readValue(confFile, Map.class);
        } catch (IOException e) {
            Utils.log(String.format("File %s is not a valid configuration json file!%n%s",
                    confFile.getAbsolutePath(), e.toString()));
        }
        return new HashMap<>();
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
