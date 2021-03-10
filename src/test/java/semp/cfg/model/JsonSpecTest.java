package semp.cfg.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class JsonSpecTest {
    static private final ObjectMapper objectMapper = new ObjectMapper();
    static private JsonSpec jsonSpec;

    @BeforeAll
    static void setup() throws IOException {
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
    void testPathExist(String path, boolean isExist) {
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


    @ParameterizedTest
    @CsvSource({
            "/dmrClusters, /dmrClusters/{dmrClusterName}",
            "/msgVpns/{msgVpnName}/aclProfiles, /msgVpns/{msgVpnName}/aclProfiles/{aclProfileName}",
            "/msgVpns/{msgVpnName}/aclProfiles/{aclProfileName}/publishTopicExceptions, '/msgVpns/{msgVpnName}/aclProfiles/{aclProfileName}/publishTopicExceptions/{publishTopicExceptionSyntax},{publishTopicException}'"
    })
    void testGetObjectPath(String collectionPath, String objectPath) {
        assertEquals(objectPath, jsonSpec.getObjectPath(collectionPath));
    }

    @ParameterizedTest
    @CsvSource({
            "/dmrClusters---",
            "/msgVpns/{msgV{pnName}/aclProfiles"
    })
    void testGetObjectPathException(String collectionPath) {
        assertThrows(NoSuchElementException.class, () -> jsonSpec.getObjectPath(collectionPath));
    }

    static Stream<Arguments> stringIntAndListProvider() {
        return Stream.of(
                arguments("/dmrClusters/{dmrClusterName}",
                        Collections.singletonList("dmrClusterName")),
                arguments("/msgVpns/{msgVpnName}/aclProfiles/{aclProfileName}",
                        Collections.singletonList("aclProfileName")),
                arguments("/msgVpns/{msgVpnName}/aclProfiles/{aclProfileName}/publishTopicExceptions/{publishTopicExceptionSyntax},{publishTopicException}",
                        Arrays.asList("publishTopicExceptionSyntax", "publishTopicException")),
                arguments("/msgVpns/{msgVpnName}/bridges/{bridgeName},{bridgeVirtualRouter}/remoteMsgVpns/{remoteMsgVpnName},{remoteMsgVpnLocation},{remoteMsgVpnInterface}",
                        Arrays.asList("remoteMsgVpnName", "remoteMsgVpnLocation", "remoteMsgVpnInterface"))
        );
    }
    @ParameterizedTest
    @MethodSource("stringIntAndListProvider")
    void testGenerateIdentifiers(String objectPath, List<String> idsList) {
        assertEquals(idsList, JsonSpec.generateIdentifiers(objectPath));
    }
}
