package semp.cfg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import semp.cfg.model.SempResponse;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SempClient {
    private static final Logger logger = LoggerFactory.getLogger(SempClient.class);
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
        HttpRequest request = HttpRequest.newBuilder()
                .method(method.toUpperCase(), Objects.isNull(payload)? BodyPublishers.noBody() : BodyPublishers.ofString(payload))
                .uri(URI.create(absUri))
                .header("content-type", "application/json")
                .build();
        JsonNode node = null;
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            node = objectMapper.readTree(response.body());
        } catch (InterruptedException|IOException e) {
            logger.error("{} on [{}] with playload:\n{}\n{}", method.toUpperCase(), absUri,
                    payload, e.toString());
            System.exit(1);
        }
        return node;
    }
    protected JsonNode sendWithResourcePath(String method, String resourcePath, String payload){
        return sendWithAbsoluteURI(method, buildAbsoluteUri(resourcePath), payload);
    }
}
