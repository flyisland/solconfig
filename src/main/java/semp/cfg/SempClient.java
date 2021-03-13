package semp.cfg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

public class SempClient {
    private static final String CONFIG_BASE_PATH = "/SEMP/v2/config";
    private static final ObjectMapper objectMapper = new ObjectMapper();

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
                SempResponse resp = SempResponse.ofJsonNode(sendWithAbsoluteURI("GET", nextPageUri.get(), null));
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

    public Optional<SempResponse> getCollectionWithResourcePath(String resourcePath){
        return getCollectionWithAbsoluteUri(buildAbsoluteUri(resourcePath));
    }

    public String buildAbsoluteUri(String resourcePath){
        return baseUrl+resourcePath;
    }

    private JsonNode sendWithAbsoluteURI(String method, String absUri, String payload) {
        var bp = Objects.isNull(payload) || payload.isEmpty() ?
                BodyPublishers.noBody() :
                BodyPublishers.ofString(payload);
        HttpRequest request = HttpRequest.newBuilder()
                .method(method.toUpperCase(), bp)
                .uri(URI.create(absUri))
                .header("content-type", "application/json")
                .build();
        JsonNode node = null;
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            node = objectMapper.readTree(response.body());
        } catch (InterruptedException|IOException e) {
            Utils.log(String.format("%s %s with playload:%n%s%n%s",
                    method.toUpperCase(), absUri, payload, e.toString()));
            System.exit(1);
        }
        return node;
    }

    public JsonNode sendWithResourcePath(String method, String resourcePath, String payload){
        return sendWithAbsoluteURI(method, buildAbsoluteUri(resourcePath), payload);
    }

    /**
     * Run Create/Update/Delete methods on the resourcePath
     * @param method One of [post, put, patch, delete]
     * @return the SEMP meta result
     */
    public SempMeta cudWithResourcePath(String method, String resourcePath, String payload) {
        var resp = sendWithResourcePathStr(method, resourcePath, payload);
        if (resp.isPresent()) {
            return SempMeta.ofString(resp.get());
        } else {
            Utils.err("%s %s returns nothing!", method, buildAbsoluteUri(resourcePath));
            System.exit(1);
            return null;
        }
    }

    private Optional<String> sendWithResourcePathStr(String method, String resourcePath, String payload) {
        return sendWithAbsoluteURIStr(method, buildAbsoluteUri(resourcePath), payload);
    }

    private Optional<String> sendWithAbsoluteURIStr(String method, String absUri, String payload) {
        var bp = Objects.isNull(payload) || payload.isEmpty() ?
                BodyPublishers.noBody() :
                BodyPublishers.ofString(payload);
        HttpRequest request = HttpRequest.newBuilder()
                .method(method.toUpperCase(), bp)
                .uri(URI.create(absUri))
                .header("content-type", "application/json")
                .build();

        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return Optional.of(response.body());
        } catch (InterruptedException|IOException e) {
            Utils.log(String.format("%s %s with playload:%n%s%n%s",
                    method.toUpperCase(), absUri, payload, e.toString()));
            System.exit(1);
        }
        return Optional.empty();
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

}
