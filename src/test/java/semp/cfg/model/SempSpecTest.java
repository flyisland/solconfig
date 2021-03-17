package semp.cfg.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

}
