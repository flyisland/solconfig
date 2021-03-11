package semp.cfg.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SempSpec {
    static final Logger logger = LoggerFactory.getLogger(SempSpec.class);
    static public final Map<String, String> TOP_RESOURCES = Map.of("vpn", "msgVpns", "cluster", "dmrClusters", "ca", "certAuthorities");
    static public final String SEMP_VERSION = "sempVersion";

    static private JsonSpec jsonSpec;
    static private Map<String, SempSpec> sempSpecMap = new TreeMap<>();

    private boolean deprecated = false;
    private List<String> identifiers;
    private Map<String, List<String>> specialAttributes;
    private Map<String, ?> defaultValues;

    public static void ofJsonNode(JsonNode root){
        jsonSpec = JsonSpec.ofJsonNode(root);
        TOP_RESOURCES.values().forEach(s -> buildSempSpec("", s));
    }

    private static void buildSempSpec(String parentObjectPath, String collectionName){
        new SempSpec().build(parentObjectPath, collectionName);
    }

    private void build(String parentObjectPath, String collectionName) {
        var collectionPath = parentObjectPath + "/" + collectionName;
        var objectPath = jsonSpec.getObjectPath(collectionPath);
        jsonSpec.assertPathExist(collectionPath);

        deprecated = jsonSpec.isDeprecatedCollection(collectionPath);
        identifiers = JsonSpec.generateIdentifiers(objectPath);
        specialAttributes = jsonSpec.findSpecialAttributes(collectionPath);
        defaultValues = jsonSpec.getMapOfAttributesWithDefaultValue(collectionPath);

        var childrenNames = jsonSpec.getChildrenNames(objectPath);

//        sempSpecMap.put(collectionPath, sempSpec);

    }

    public boolean isDeprecatedObject(){
        return deprecated;
    }

    public boolean isDeprecatedAttribute(String name){
        return false;
    }
}
