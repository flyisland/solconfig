package semp.cfg.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.Setter;
import semp.cfg.Utils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static semp.cfg.Utils.objectMapper;

@Getter
@Setter
public class SempResponse {
    private List<Map<String, Object>> data;
    private List<Map<String, String >> links;
    private SempMeta meta;

    public static SempResponse ofString(String content){
        SempResponse resp = new SempResponse();
        try {
            var node = objectMapper.readTree(content);
            resp.data = objectMapper.treeToValue(node.get("data"), List.class);
            resp.links = objectMapper.treeToValue(node.get("links"), List.class);
            resp.meta = objectMapper.treeToValue(node.get("meta"), SempMeta.class);
        } catch (JsonProcessingException e) {
            Utils.errPrintlnAndExit(e,
                    "Unable to convert below string into SempResponse structure!%n%s",
                    content);
        }
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
