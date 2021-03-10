package semp.cfg.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonSpecTest {
    static private ObjectMapper objectMapper = new ObjectMapper();
    static private JsonSpec jsonSpec;

    @BeforeAll static void setup() throws IOException {
        JsonNode jsonNode = objectMapper.readTree(
                JsonSpecTest.class.getResource("/semp-v2-config-2.19.json"));
        jsonSpec = JsonSpec.ofJsonNode(jsonNode);
    }

    @ParameterizedTest
    @ValueSource(strings = { "/",
            "/dmrClusters/{dmrClusterName}/links/{remoteNodeName}",
            "/msgVpns" })
    void testPathExist(String path){
        assertTrue(jsonSpec.isPathExist(path));
    }

    @ParameterizedTest
    @ValueSource(strings = { "this is a test",
            "/dmrClusters/links",
            "/queues" })
    void testPathNotExist(String path){
        assertFalse(jsonSpec.isPathExist(path));
    }

}
