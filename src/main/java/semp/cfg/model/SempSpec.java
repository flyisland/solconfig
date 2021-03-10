package semp.cfg.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.TreeMap;

public class SempSpec {
    static final Logger logger = LoggerFactory.getLogger(SempSpec.class);
    static public final Map<String, String> TOP_RESOURCES = Map.of("vpn", "msgVpns", "cluster", "dmrClusters", "ca", "certAuthorities");
    static public final String SEMP_VERSION = "sempVersion";

    static private Map<String, SempSpec> sempSpecMap = new TreeMap<>();
    static private Map<String, Object> specPathsMap;

    static public void ofMap(Map<String, Object> spec){
        specPathsMap = (Map<String, Object>) spec.get("paths");
        TOP_RESOURCES.values().forEach(s -> {
            buildSempSpec("", s);

        });
    }

    private static void buildSempSpec(String parentSpecPath, String collectionName){
        SempSpec sempSpec = new SempSpec();
        String thiSpecPath = parentSpecPath + "/" + collectionName;

        sempSpecMap.put(thiSpecPath, sempSpec);
    }

    private void assertPathExist(String specPath){
        if (! specPathsMap.containsKey(specPath)){
            logger.error("Path '{}' is NOT found inside the SEMP specification!", specPath);
            System.exit(1);
        }
    }

    public boolean isDeprecatedObject(){
        return false;
    }

    public boolean isDeprecatedAttribute(String name){
        return false;
    }
}
