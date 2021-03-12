package semp.cfg.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SempSpec {
    public static  final String BROKER_SPEC_PATH = "";
    public static final Map<String, String> TOP_RESOURCES = Map.of("vpn", "msgVpns", "cluster", "dmrClusters", "ca", "certAuthorities");
    public static final String SEMP_VERSION = "sempVersion";

    private static JsonSpec jsonSpec;
    protected static Map<String, SempSpec> sempSpecMap = new TreeMap<>();

    private String specPath;
    private boolean deprecated = false;
    @Getter
    private List<String> identifiers;
    private Map<String, List<String>> specialAttributes;
    private Map<String, ?> defaultValues;
    private List<String> childrenNames;

    // TODO: rename to stup
    public static void ofJsonNode(JsonNode root){
        jsonSpec = JsonSpec.ofJsonNode(root);
        TOP_RESOURCES.values().forEach(s -> buildSempSpec("", s));

        sempSpecMap.put(BROKER_SPEC_PATH, brokerSpec());
    }

    private static SempSpec brokerSpec() {
        var spec = new SempSpec();
        spec.specPath = BROKER_SPEC_PATH;
        spec.identifiers = new LinkedList<>();
        return spec;
    }

    private static void buildSempSpec(String parentObjectPath, String collectionName){
         var spec = SempSpec.of(parentObjectPath, collectionName);
        sempSpecMap.put(spec.specPath, spec);

        var objectPath = jsonSpec.getObjectPath(parentObjectPath + "/" + collectionName);
        spec.childrenNames.forEach(name -> buildSempSpec(objectPath, name));
    }

    private static SempSpec of(String parentObjectPath, String collectionName) {
        var spec = new SempSpec();
        var collectionPath = parentObjectPath + "/" + collectionName;
        var objectPath = jsonSpec.getObjectPath(collectionPath);
        jsonSpec.assertPathExist(collectionPath);

        spec.specPath = generateSpecPath(collectionPath);
        spec.deprecated = jsonSpec.isDeprecatedCollection(collectionPath);
        spec.identifiers = JsonSpec.generateIdentifiers(objectPath);
        spec.specialAttributes = jsonSpec.findSpecialAttributes(collectionPath);
        spec.defaultValues = jsonSpec.getMapOfAttributesWithDefaultValue(collectionPath);
        spec.childrenNames = jsonSpec.getChildrenNames(objectPath);

        return spec;
    }

    protected static String generateSpecPath(String path) {
        var names = path.split("/");
        var sb = new StringBuilder();
        for (int i = 1; i < names.length; i++) {
            if (i%2 == 1) sb.append("/").append(names[i]);
        }
        return sb.toString();
    }

    public static String getTopResourceIdentifierKey(String resourceName) {
        if (TOP_RESOURCES.containsValue(resourceName)){
            return sempSpecMap.get("/"+resourceName).identifiers.get(0);
        }else{
            throw new IllegalArgumentException(
                    String.format("%s is NOT one of [%s]!",
                            resourceName, TOP_RESOURCES.values()));
        }
    }

    protected static SempSpec get(String specPath) {
        return sempSpecMap.get(specPath);
    }
}
