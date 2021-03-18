package com.solace.tools.sempcfg.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.Setter;
import com.solace.tools.sempcfg.Utils;

import static com.solace.tools.sempcfg.Utils.objectMapper;

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
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return e.toString();
        }
    }

    public static SempMeta ofString(String input) {
        try {
            var node = objectMapper.readTree(input);
            return objectMapper.treeToValue(node.get("meta"), SempMeta.class);
        } catch (JsonProcessingException e) {
            Utils.errPrintlnAndExit(
                    e, "Unable convert below text into a valid SempMeta structure.%n%s", input);
            return null;
        }
    }
}
