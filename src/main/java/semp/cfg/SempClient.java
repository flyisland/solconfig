package semp.cfg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import semp.cfg.model.SempResponse;
import semp.cfg.model.SempVersion;

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
    private static final String CONFIG_BASE_PATH ="/SEMP/v2/config";
    private final String baseUrl;
    private final String base64Auth;
    private final HttpClient httpClient;
    private static ObjectMapper objectMapper = new ObjectMapper();

    public SempClient(String adminUrl, String adminUser, String adminPwd) {
        this.baseUrl = adminUrl+ CONFIG_BASE_PATH;
        httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        base64Auth = "Basic " + Base64.getEncoder().encodeToString((adminUser + ":" + adminPwd).getBytes());
    }

    public SempVersion getSempVersion(){
        JsonNode node = sendWithResourcePath("GET", "/about/api", null);
        return new SempVersion(node.get("data").get("sempVersion").asText());
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
        HttpRequest request = HttpRequest.newBuilder()
                .method(method, Objects.isNull(payload)? BodyPublishers.noBody() : BodyPublishers.ofString(payload))
                .uri(URI.create(absUri))
                .header("Authorization", base64Auth)
                .header("content-type", "application/json")
                .build();
        JsonNode node = null;
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            node = objectMapper.readTree(response.body());
        } catch (InterruptedException|IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return node;
    }
    private JsonNode sendWithResourcePath(String method, String resourcePath, String payload){
        return sendWithAbsoluteURI(method, buildAbsoluteUri(resourcePath), payload);
    }
}
