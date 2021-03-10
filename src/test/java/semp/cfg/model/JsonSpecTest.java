package semp.cfg.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonSpecTest {
    static private ObjectMapper objectMapper = new ObjectMapper();
    static private JsonSpec jsonSpec;

    @BeforeAll static void setup() throws IOException {
        JsonNode jsonNode = objectMapper.readTree(
                JsonSpecTest.class.getResource("/semp-v2-config-2.19.json"));
        jsonSpec = JsonSpec.ofJsonNode(jsonNode);
    }

    @ParameterizedTest
    @CsvSource({
            "/certAuthorities, true",
            "/dmrClusters/{dmrClusterName}/links/{remoteNodeName}/tlsTrustedCommonNames, true",
            "/msgVpns/{msgVpnName}/aclProfiles/{aclProfileName}/subscribeExceptions, true",
            "/this is a test, false",
            "/queues, false",
            "/dmrClusters/links, false"
    })
    void testPathExist(String path, boolean isExist){
        assertEquals(isExist, jsonSpec.isPathExist(path));
    }

    @ParameterizedTest
    @CsvSource({
            "/certAuthorities, true",
            "/dmrClusters/{dmrClusterName}/links/{remoteNodeName}/tlsTrustedCommonNames, true",
            "/msgVpns/{msgVpnName}/aclProfiles/{aclProfileName}/subscribeExceptions, true",
            "/msgVpns, false",
            "/msgVpns/{msgVpnName}/aclProfiles, false"
    })
    void testisDeprecatedCollection(String path, boolean isDeprecated) {
        assertEquals(isDeprecated, jsonSpec.isDeprecatedCollection(path));
    }
}
