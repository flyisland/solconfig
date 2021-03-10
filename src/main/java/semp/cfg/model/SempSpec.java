package semp.cfg.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.TreeMap;

public class SempSpec {
    static final Logger logger = LoggerFactory.getLogger(SempSpec.class);
    static public final Map<String, String> TOP_RESOURCES = Map.of("vpn", "msgVpns", "cluster", "dmrClusters", "ca", "certAuthorities");
    static public final String SEMP_VERSION = "sempVersion";

    static private JsonSpec jsonSpec;
    static private Map<String, SempSpec> sempSpecMap = new TreeMap<>();

    private boolean deprecated = false;

    public static void ofJsonNode(JsonNode root){
        jsonSpec = JsonSpec.ofJsonNode(root);
        TOP_RESOURCES.values().forEach(s -> buildSempSpec("", s));
    }

    private static void buildSempSpec(String parentObjectPath, String collectionName){
        SempSpec sempSpec = new SempSpec();
        String specCollectionPath = parentObjectPath + "/" + collectionName;
        jsonSpec.assertPathExist(specCollectionPath);
        sempSpec.deprecated = jsonSpec.isDeprecatedCollection(specCollectionPath);
//        sempSpecMap.put(collectionPath, sempSpec);
    }

    public boolean isDeprecatedObject(){
        return false;
    }

    public boolean isDeprecatedAttribute(String name){
        return false;
    }
}
