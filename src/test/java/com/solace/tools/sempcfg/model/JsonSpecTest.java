package com.solace.tools.sempcfg.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.solace.tools.sempcfg.Utils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static com.solace.tools.sempcfg.Utils.objectMapper;

public class JsonSpecTest {
    private static JsonSpec jsonSpec;
    private static Object jsonDocument;

    @BeforeAll
    static void setup() throws IOException {
        var jsonString = Files.readString(Path.of(JsonSpecTest.class.getResource("/semp-v2-config-2.19.json").getPath()));
        jsonDocument = Configuration.defaultConfiguration().jsonProvider().parse(jsonString);
        jsonSpec = JsonSpec.ofString(jsonString);
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
            "/msgVpns/{msgVpnName}/aclProfiles/{aclProfileName}/subscribeTopicExceptions, '{\n" +
                    "  \"Read-Only\" : [ \"aclProfileName\", \"msgVpnName\" ],\n" +
                    "  \"ALL\" : [ \"aclProfileName\", \"msgVpnName\", \"subscribeTopicException\", \"subscribeTopicExceptionSyntax\" ],\n" +
                    "  \"Parent-Identifiers\" : [ \"aclProfileName\", \"msgVpnName\" ],\n" +
                    "  \"Required\" : [ \"subscribeTopicException\", \"subscribeTopicExceptionSyntax\" ],\n" +
                    "  \"Deprecated\" : [ ],\n" +
                    "  \"Opaque\" : [ ],\n" +
                    "  \"Identifying\" : [ \"subscribeTopicExceptionSyntax\", \"subscribeTopicException\" ],\n" +
                    "  \"Write-Only\" : [ ]\n" +
                    "}'",
            "'/msgVpns/{msgVpnName}/bridges/{bridgeName},{bridgeVirtualRouter}/remoteMsgVpns', '{\n" +
                    "  \"Read-Only\" : [ \"bridgeName\", \"bridgeVirtualRouter\", \"msgVpnName\", \"remoteMsgVpnInterface\", \"remoteMsgVpnLocation\", \"remoteMsgVpnName\" ],\n" +
                    "  \"Requires-Disable\" : [ \"clientUsername\", \"compressedDataEnabled\", \"egressFlowWindowSize\", \"password\", \"tlsEnabled\" ],\n" +
                    "  \"ALL\" : [ \"bridgeName\", \"bridgeVirtualRouter\", \"clientUsername\", \"compressedDataEnabled\", \"connectOrder\", \"egressFlowWindowSize\", \"enabled\", \"msgVpnName\", \"password\", \"queueBinding\", \"remoteMsgVpnInterface\", \"remoteMsgVpnLocation\", \"remoteMsgVpnName\", \"tlsEnabled\", \"unidirectionalClientProfile\" ],\n"+
                    "  \"Parent-Identifiers\" : [ \"bridgeName\", \"bridgeVirtualRouter\", \"msgVpnName\" ],\n" +
                    "  \"Required\" : [ \"remoteMsgVpnLocation\", \"remoteMsgVpnName\" ],\n" +
                    "  \"Deprecated\" : [ ],\n" +
                    "  \"Opaque\" : [ \"password\" ],\n" +
                    "  \"Identifying\" : [ \"remoteMsgVpnName\", \"remoteMsgVpnLocation\", \"remoteMsgVpnInterface\" ],\n" +
                    "  \"Write-Only\" : [ \"password\" ]\n" +
                    "}'"
    })
    void testFindSpecialAttributes(String path, String expected) throws JsonProcessingException {
        var m1 = jsonSpec.findAttributes(path);
        var m2 = objectMapper.readValue(expected, Map.class);
        assertEquals(Utils.toPrettyJson(m2), Utils.toPrettyJson(m1));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "$.paths.keys()",
            "$.paths./msgVpns.post.parameters[?(@.name=='body')].schema.$ref",
            "$.paths./msgVpns/{msgVpnName}/aclProfiles/{aclProfileName}.patch.description"
    })
    void testJsonPath(String path){
        Configuration conf = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS);
        System.out.println(path + " -> " + JsonPath.using(conf).parse(jsonDocument).read(path));
    }


    @ParameterizedTest
    @CsvSource({
            "'The audience claim name, indicating which part of the object to use for determining the audience. The default value is `\"aud\"`.', \"aud\""
    })
    void testFindDefaultValue(String description, String expected) {
        assertEquals(Optional.of(expected), JsonSpec.findDefaultValue(description));
    }

    @ParameterizedTest
    @CsvSource({
            "/msgVpns/{msgVpnName}, '[aclProfiles, authenticationOauthProviders, authorizationGroups, bridges, clientProfiles, clientUsernames, distributedCaches, dmrBridges, jndiConnectionFactories, jndiQueues, jndiTopics, mqttRetainCaches, mqttSessions, queueTemplates, queues, replayLogs, replicatedTopics, restDeliveryPoints, sequencedTopics, topicEndpointTemplates, topicEndpoints]'",
            "/msgVpns/{msgVpnName}/aclProfiles/{aclProfileName}, '[clientConnectExceptions, publishExceptions, publishTopicExceptions, subscribeExceptions, subscribeShareNameExceptions, subscribeTopicExceptions]'"
    })
    void testGetChildrenNames(String objectPath, String expected) {
        assertEquals(expected, jsonSpec.getChildrenNames(objectPath).toString());
    }

    @ParameterizedTest
    @MethodSource("testFindAttributesCombinationsFromDescriptionProvider")
    void testFindAttributesCombinationsFromDescription(String path, String expected) {
        var description = jsonSpec.getDescriptionOfAction(path, HTTPMethod.POST);
        var result = jsonSpec.findAttributesCombinationsFromDescription(description);
        assertEquals(expected, Utils.toPrettyJson(result));
    }

    static Stream<Arguments> testFindAttributesCombinationsFromDescriptionProvider() {
        return Stream.of(
                arguments("/msgVpns", "{\n" +
                        "  \"AttributeCombinationKey{sempClassName='EventThresholdByValue', attributeName='clearValue', type=Requires}\" : [ \"setValue\" ],\n" +
                        "  \"AttributeCombinationKey{sempClassName='EventThresholdByValue', attributeName='setValue', type=Requires}\" : [ \"clearValue\" ],\n" +
                        "  \"AttributeCombinationKey{sempClassName='EventThreshold', attributeName='clearPercent', type=Conflicts}\" : [ \"clearValue\", \" setValue\" ],\n" +
                        "  \"AttributeCombinationKey{sempClassName='EventThreshold', attributeName='clearPercent', type=Requires}\" : [ \"setPercent\" ],\n" +
                        "  \"AttributeCombinationKey{sempClassName='EventThreshold', attributeName='clearValue', type=Conflicts}\" : [ \"clearPercent\", \" setPercent\" ],\n" +
                        "  \"AttributeCombinationKey{sempClassName='EventThreshold', attributeName='clearValue', type=Requires}\" : [ \"setValue\" ],\n" +
                        "  \"AttributeCombinationKey{sempClassName='EventThreshold', attributeName='setPercent', type=Conflicts}\" : [ \"clearValue\", \" setValue\" ],\n" +
                        "  \"AttributeCombinationKey{sempClassName='EventThreshold', attributeName='setPercent', type=Requires}\" : [ \"clearPercent\" ],\n" +
                        "  \"AttributeCombinationKey{sempClassName='EventThreshold', attributeName='setValue', type=Conflicts}\" : [ \"clearPercent\", \" setPercent\" ],\n" +
                        "  \"AttributeCombinationKey{sempClassName='EventThreshold', attributeName='setValue', type=Requires}\" : [ \"clearValue\" ],\n" +
                        "  \"AttributeCombinationKey{sempClassName='MsgVpn', attributeName='authenticationBasicProfileName', type=Requires}\" : [ \"authenticationBasicType\" ],\n" +
                        "  \"AttributeCombinationKey{sempClassName='MsgVpn', attributeName='authorizationProfileName', type=Requires}\" : [ \"authorizationType\" ],\n" +
                        "  \"AttributeCombinationKey{sempClassName='MsgVpn', attributeName='eventPublishTopicFormatMqttEnabled', type=Requires}\" : [ \"eventPublishTopicFormatSmfEnabled\" ],\n" +
                        "  \"AttributeCombinationKey{sempClassName='MsgVpn', attributeName='eventPublishTopicFormatSmfEnabled', type=Requires}\" : [ \"eventPublishTopicFormatMqttEnabled\" ],\n" +
                        "  \"AttributeCombinationKey{sempClassName='MsgVpn', attributeName='replicationBridgeAuthenticationBasicClientUsername', type=Requires}\" : [ \"replicationBridgeAuthenticationBasicPassword\" ],\n" +
                        "  \"AttributeCombinationKey{sempClassName='MsgVpn', attributeName='replicationBridgeAuthenticationBasicPassword', type=Requires}\" : [ \"replicationBridgeAuthenticationBasicClientUsername\" ],\n" +
                        "  \"AttributeCombinationKey{sempClassName='MsgVpn', attributeName='replicationBridgeAuthenticationClientCertPassword', type=Requires}\" : [ \"replicationBridgeAuthenticationClientCertContent\" ],\n" +
                        "  \"AttributeCombinationKey{sempClassName='MsgVpn', attributeName='replicationEnabledQueueBehavior', type=Requires}\" : [ \"replicationEnabled\" ]\n" +
                        "}"),
                arguments("/msgVpns/{msgVpnName}/restDeliveryPoints", "{ }"),
                arguments("/msgVpns/{msgVpnName}/restDeliveryPoints/{restDeliveryPointName}/restConsumers", "{\n" +
                        "  \"AttributeCombinationKey{sempClassName='MsgVpnRestDeliveryPointRestConsumer', attributeName='authenticationClientCertPassword', type=Requires}\" : [ \"authenticationClientCertContent\" ],\n" +
                        "  \"AttributeCombinationKey{sempClassName='MsgVpnRestDeliveryPointRestConsumer', attributeName='authenticationHttpBasicPassword', type=Requires}\" : [ \"authenticationHttpBasicUsername\" ],\n" +
                        "  \"AttributeCombinationKey{sempClassName='MsgVpnRestDeliveryPointRestConsumer', attributeName='authenticationHttpBasicUsername', type=Requires}\" : [ \"authenticationHttpBasicPassword\" ],\n" +
                        "  \"AttributeCombinationKey{sempClassName='MsgVpnRestDeliveryPointRestConsumer', attributeName='remotePort', type=Requires}\" : [ \"tlsEnabled\" ],\n" +
                        "  \"AttributeCombinationKey{sempClassName='MsgVpnRestDeliveryPointRestConsumer', attributeName='tlsEnabled', type=Requires}\" : [ \"remotePort\" ]\n" +
                        "}")
        );
    }
}
