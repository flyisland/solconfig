package semp.cfg.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.*;
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

    static Stream<Arguments> testGenerateIdentifiersProvider() {
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
    @MethodSource("testGenerateIdentifiersProvider")
    void testGenerateIdentifiers(String objectPath, List<String> idsList) {
        assertEquals(idsList, JsonSpec.generateIdentifiers(objectPath));
    }

    @ParameterizedTest
    @CsvSource({
            "/msgVpns, '{\n" +
                    "  \"Read-Only\" : [ \"msgVpnName\" ],\n" +
                    "  \"Requires-Disable\" : [ ],\n" +
                    "  \"Deprecated\" : [ \"bridgingTlsServerCertEnforceTrustedCommonNameEnabled\", \"restTlsServerCertEnforceTrustedCommonNameEnabled\" ],\n" +
                    "  \"Opaque\" : [ \"replicationBridgeAuthenticationBasicPassword\", \"replicationBridgeAuthenticationClientCertContent\" ],\n" +
                    "  \"Identifying\" : [ \"msgVpnName\" ],\n" +
                    "  \"Write-Only\" : [ \"replicationBridgeAuthenticationBasicPassword\", \"replicationBridgeAuthenticationClientCertContent\", \"replicationBridgeAuthenticationClientCertPassword\", \"replicationEnabledQueueBehavior\" ]\n" +
                    "}'",
            "/msgVpns/{msgVpnName}/aclProfiles/{aclProfileName}/subscribeTopicExceptions, '{\n" +
                    "  \"Read-Only\" : [ \"aclProfileName\", \"msgVpnName\" ],\n" +
                    "  \"Required\" : [ \"subscribeTopicException\", \"subscribeTopicExceptionSyntax\" ],\n" +
                    "  \"Deprecated\" : [ ],\n" +
                    "  \"Opaque\" : [ ],\n" +
                    "  \"Identifying\" : [ \"aclProfileName\", \"msgVpnName\", \"subscribeTopicException\", \"subscribeTopicExceptionSyntax\" ],\n" +
                    "  \"Write-Only\" : [ ]\n" +
                    "}'",
            "/dmrClusters, '{\n" +
                    "  \"Read-Only\" : [ \"directOnlyEnabled\", \"dmrClusterName\", \"nodeName\" ],\n" +
                    "  \"Requires-Disable\" : [ \"authenticationBasicPassword\", \"authenticationClientCertContent\", \"authenticationClientCertPassword\" ],\n" +
                    "  \"Deprecated\" : [ \"tlsServerCertEnforceTrustedCommonNameEnabled\" ],\n" +
                    "  \"Opaque\" : [ \"authenticationBasicPassword\", \"authenticationClientCertContent\" ],\n" +
                    "  \"Identifying\" : [ \"dmrClusterName\" ],\n" +
                    "  \"Write-Only\" : [ \"authenticationBasicPassword\", \"authenticationClientCertContent\", \"authenticationClientCertPassword\" ]\n" +
                    "}'",
            "/dmrClusters/{dmrClusterName}/links, '{\n" +
                    "  \"Read-Only\" : [ \"dmrClusterName\", \"remoteNodeName\" ],\n" +
                    "  \"Requires-Disable\" : [ \"authenticationBasicPassword\", \"authenticationScheme\", \"egressFlowWindowSize\", \"initiator\", \"span\", \"transportCompressedEnabled\", \"transportTlsEnabled\" ],\n" +
                    "  \"Deprecated\" : [ ],\n" +
                    "  \"Opaque\" : [ \"authenticationBasicPassword\" ],\n" +
                    "  \"Identifying\" : [ \"dmrClusterName\", \"remoteNodeName\" ],\n" +
                    "  \"Write-Only\" : [ \"authenticationBasicPassword\" ]\n" +
                    "}'"
    })
    void testFindSpecialAttributes(String path, String expected) throws JsonProcessingException {
        var m1 = jsonSpec.findSpecialAttributes(path);
        var m2 = objectMapper.readValue(expected, Map.class);
        assertEquals(m2, m1);
    }
}
