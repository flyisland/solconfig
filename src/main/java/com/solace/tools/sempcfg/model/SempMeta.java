package com.solace.tools.sempcfg.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import com.solace.tools.sempcfg.Utils;

import java.util.Objects;
import java.util.Optional;

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

        public static SempError ofJsonNode(JsonNode node) {
            if (Objects.isNull(node)) {
                return null;
            }
            var error = new SempError();
            error.code = Optional.ofNullable(node.get("code")).map(JsonNode::asInt).orElse(-1);
            error.description = Optional.ofNullable((node.get("description"))).map(JsonNode::asText).orElse("");
            error.status = Optional.ofNullable((node.get("status"))).map(JsonNode::asText).orElse("");
            return error;
        }
    }

    @Getter
    @Setter
    public static class SempPaging {
        private String cursorQuery;
        private String nextPageUri;

        public static SempPaging ofJsonNode(JsonNode node) {
            if (Objects.isNull(node)) {
                return null;
            }
            var paging = new SempPaging();
            paging.cursorQuery = Optional.ofNullable((node.get("cursorQuery"))).map(JsonNode::asText).orElse("");
            paging.nextPageUri = Optional.ofNullable((node.get("nextPageUri"))).map(JsonNode::asText).orElse("");
            return paging;
        }
    }

    @Getter
    @Setter
    public static class SempRequest {
        private String method;
        private String uri;

        public static SempRequest ofJsonNode(JsonNode node) {
            if (Objects.isNull(node)) {
                return null;
            }
            var request = new SempRequest();
            request.method = Optional.ofNullable((node.get("method"))).map(JsonNode::asText).orElse("");
            request.uri = Optional.ofNullable((node.get("uri"))).map(JsonNode::asText).orElse("");
            return request;
        }

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
            return ofJsonNode(node);
        } catch (JsonProcessingException e) {
            Utils.errPrintlnAndExit(
                    e, "Unable convert below text into a valid SempMeta structure.%n%s", input);
            return null;
        }
    }

    public static SempMeta ofJsonNode(JsonNode node) {
        if (Objects.isNull(node)) {
            return null;
        }
        SempMeta meta = new SempMeta();
        meta.error = SempError.ofJsonNode(node.get("error"));
        meta.paging = SempPaging.ofJsonNode(node.get("paging"));
        meta.request = SempRequest.ofJsonNode(node.get("request"));
        meta.responseCode = Optional.ofNullable(node.get("responseCode")).map(JsonNode::asInt).orElse(-1);
        return meta;
    }
}
