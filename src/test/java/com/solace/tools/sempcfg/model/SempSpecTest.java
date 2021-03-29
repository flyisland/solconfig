package com.solace.tools.sempcfg.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class SempSpecTest {
    @BeforeAll
    static void setup() throws IOException {
        var jsonString = Files.readString(Path.of(JsonSpecTest.class.getResource("/semp-v2-config-2.19.json").getPath()));
        SempSpec.setupByString(jsonString);
    }

    @ParameterizedTest
    @CsvSource({
            "/dmrClusters, /dmrClusters",
            "/msgVpns/{msgVpnName}/aclProfiles, /msgVpns/aclProfiles",
            "'/msgVpns/{msgVpnName}/aclProfiles/{aclProfileName}/publishTopicExceptions/{publishTopicExceptionSyntax},{publishTopicException}', /msgVpns/aclProfiles/publishTopicExceptions"
    })
    void testGenerateSpecPath(String path, String expected) {
        assertEquals(expected, SempSpec.generateSpecPath(path));
    }


    @Test
    void testOfJsonNode() {
        SempSpec.sempSpecMap.keySet().forEach(System.out::println);
    }

    @ParameterizedTest
    @CsvSource({
            "dmrClusters, dmrClusterName",
            "msgVpns, msgVpnName"
    })
    void testGetTopResourceIdentifierKey(String topName, String expected) {
        assertEquals(expected, SempSpec.getTopResourceIdentifierKey(topName));
    }

    @ParameterizedTest
    @MethodSource("testGetRequiresAttributeWithDefaultValueProvider")
    void testGetRequiresAttributeWithDefaultValue(String specPath, Set<String> attributes, Map<String, Object> expected){
        var sempSpec = SempSpec.sempSpecMap.get(specPath);
        var result = sempSpec.getRequiresAttributeWithDefaultValue(attributes);
        assertEquals(expected, result);
    }

    static Stream<Arguments> testGetRequiresAttributeWithDefaultValueProvider() {
        return Stream.of(
                arguments("/msgVpns/restDeliveryPoints/restConsumers", Set.of("remotePort", "tlsEnabled"), Map.of()),
                arguments("/msgVpns/restDeliveryPoints/restConsumers", Set.of("remotePort"), Map.of("tlsEnabled", false)),
                arguments("/msgVpns/restDeliveryPoints/restConsumers", Set.of("tlsEnabled"), Map.of("remotePort", 8080)),
                arguments("/msgVpns/restDeliveryPoints/restConsumers", Set.of("restConsumerName"), Map.of()),
                arguments("/msgVpns/restDeliveryPoints/restConsumers", Set.of(), Map.of())
        );
    }
}
