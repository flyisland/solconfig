package semp.cfg.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SempMeta {
    private long count;
    private SempError error;
    private SempPaging paging;
    private SempRequest request;
    private int responseCode;

    @Getter
    @Setter
    public static class SempError {
        private int code;
        private String description;
        private String status;
    }

    @Getter
    @Setter
    public static class SempPaging {
        private String cursorQuery;
        private String nextPageUri;
    }

    @Getter
    @Setter
    public static class SempRequest {
        private String method;
        private String uri;
    }

    @Override
    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return e.toString();
        }
    }
}
