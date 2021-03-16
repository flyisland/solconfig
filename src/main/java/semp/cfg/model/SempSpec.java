package semp.cfg.model;

import lombok.Getter;

import java.util.*;

public class SempSpec {
    public static  final String BROKER_SPEC_PATH = "";
    public static final Map<String, String> TOP_RESOURCES = Map.of("vpn", "msgVpns", "cluster", "dmrClusters", "ca", "certAuthorities");
    public static final String SEMP_VERSION = "sempVersion";
    public static final String OPAQUE_PASSWORD = "opaquePassword";
    public static final String DEFAULT_OBJECT_NAME = "default";
    public static final List<String> SPEC_PATHS_OF_DEFAULT_OBJECT = List.of("/msgVpns", "/msgVpns/aclProfiles", "/msgVpns/clientProfiles", "/msgVpns/clientUsernames");
    public static final String ENABLED_ATTRIBUTE_NAME = "enabled";
    public static final List<String> SPEC_PATHS_OF_REQUIRES_DISABLE_CHILD = List.of("/dmrClusters/links/remoteAddresses");

    private static JsonSpec jsonSpec;
    protected static Map<String, SempSpec> sempSpecMap = new TreeMap<>();
    @Getter private static SempVersion sempVersion;

    @Getter private String specPath;
    @Getter private boolean deprecated;
    private Map<String, List<String>> attributes;
    @Getter private Map<String, Object> defaultValues;
    @Getter private List<String> childrenNames;

    public static void setupByString(String jsonString) {
        jsonSpec = JsonSpec.ofString(jsonString);
        TOP_RESOURCES.values().forEach(s -> buildSempSpec("", s));

        sempSpecMap.put(BROKER_SPEC_PATH, brokerSpec());
        sempVersion = new SempVersion(jsonSpec.getSempVersionText());
    }

    private static SempSpec brokerSpec() {
        var spec = new SempSpec();
        spec.specPath = BROKER_SPEC_PATH;
        spec.attributes = new HashMap<>();
        spec.defaultValues = new HashMap<>();
        spec.childrenNames = new LinkedList<>(TOP_RESOURCES.values());
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
        spec.attributes = jsonSpec.findAttributes(collectionPath);
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
            return sempSpecMap.get("/"+resourceName).
                    getAttributeNames(AttributeType.IDENTIFYING).get(0);
        }else{
            throw new IllegalArgumentException(
                    String.format("%s is NOT one of [%s]!",
                            resourceName, TOP_RESOURCES.values()));
        }
    }

    protected static SempSpec get(String specPath) {
        return sempSpecMap.get(specPath);
    }

    public List<String> getAttributeNames(AttributeType type) {
        var result = attributes.get(type.toString());
        if (Objects.nonNull(result)) {
            return result;
        }else {
            return new LinkedList<>();
        }
    }
}
