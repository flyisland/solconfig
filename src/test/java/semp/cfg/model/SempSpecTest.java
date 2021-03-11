package semp.cfg.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SempSpecTest {
    static private final ObjectMapper objectMapper = new ObjectMapper();
    static private JsonNode jsonNode;

    @BeforeAll
    static void setup() throws IOException {
        jsonNode = objectMapper.readTree(
                JsonSpecTest.class.getResource("/semp-v2-config-2.19.json"));
        SempSpec.ofJsonNode(jsonNode);
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
            "msgVpns, msgVpnName",
            "certAuthorities, certAuthorityName"
    })
    void testGetTopResourceIdentifierKey(String topName, String expected) {
        assertEquals(expected, SempSpec.getTopResourceIdentifierKey(topName));
    }

}
