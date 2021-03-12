package semp.cfg.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class SempResponse {
    private static ObjectMapper objectMapper = new ObjectMapper();

    private List<Map<String, Object>> data;
    private List<Map<String, String >> links;
    private SempMeta meta;

    public static SempResponse ofString(String content) throws JsonProcessingException {
        JsonNode node = objectMapper.readTree(content);
        return ofJsonNode(node);
    }

    public static SempResponse ofJsonNode(JsonNode node) throws JsonProcessingException {
        SempResponse resp = new SempResponse();
        resp.data = objectMapper.treeToValue(node.get("data"), List.class);
        resp.links = objectMapper.treeToValue(node.get("links"), List.class);
        resp.meta = objectMapper.treeToValue(node.get("meta"), SempMeta.class);
        return resp;
    }

    public Optional<String> getNextPageUri(){
        return Optional.of(meta).map(SempMeta::getPaging).map(SempMeta.SempPaging::getNextPageUri);
    }

    public boolean isEmpty(){
        if (Objects.isNull(data)){
            return true;
        }
        return data.isEmpty();
    }

    @Override
    public String toString() {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return e.toString();
        }
    }
}
