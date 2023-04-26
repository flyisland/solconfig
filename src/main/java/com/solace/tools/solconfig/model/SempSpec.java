package com.solace.tools.solconfig.model;

import com.solace.tools.solconfig.Utils;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

public class SempSpec {
    public static final String SKIP_THIS_OBJECT = "__skipThisObject";
    public static final String BROKER_SPEC_PATH = "";
    public static final String SEMP_VERSION = "sempVersion";
    public static final String OPAQUE_PASSWORD = "opaquePassword";
    public static final String DEFAULT_OBJECT_NAME = "default";
    public static final List<String> SPEC_PATHS_OF_DEFAULT_OBJECT = List.of("/msgVpns", "/msgVpns/aclProfiles", "/msgVpns/clientProfiles", "/msgVpns/clientUsernames");
    public static final List<String> SPEC_PATHS_OF_OBJECTS_OF_CLOUD_INSTANCE = List.of("/msgVpns", "/msgVpns/clientProfiles");
    public static final String ENABLED_ATTRIBUTE_NAME = "enabled";
    public static final List<String> SPEC_PATHS_OF_REQUIRES_DISABLE_CHILD = List.of("/msgVpns/restDeliveryPoints/restConsumers/oauthJwtClaims", "/dmrClusters/links/remoteAddresses");
    private static final Map<String, List<String>> HARD_CODE_REQUIRES_DISABLE = new HashMap<>();
    static {
        HARD_CODE_REQUIRES_DISABLE.put("/msgVpns", List.of("restTlsServerCertValidateNameEnabled"));
    }

    private static JsonSpec jsonSpec;
    protected static Map<String, SempSpec> sempSpecMap = new TreeMap<>();
    @Getter private static SempVersion sempVersion;

    @Getter private boolean deprecated;
    @Getter private Map<String, List<String>> attributes;
    @Getter private Map<String, Object> defaultValues;
    @Getter private List<String> childrenNames;
    @Getter private String sempClassName;
    @Getter private Map<AttributeCombinationKey, List<String>> attributeCombinations;

    public enum RES_ABBR {
        vpn, cluster, domainCerts, clientCerts, virtualHostname;

        private static Map<RES_ABBR, String> map = new HashMap<>();
        private static Map<String, RES_ABBR> reversedMap;
        static {
            map.put(vpn, "msgVpns");
            map.put(cluster, "dmrClusters");
            map.put(domainCerts, "domainCertAuthorities");
            map.put(clientCerts, "clientCertAuthorities");
            map.put(virtualHostname, "virtualHostnames");

            reversedMap = map.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        }

        public String getFullName() {
            return map.get(this);
        }

        public static RES_ABBR ofFullName(String fullName) {
            if (reversedMap.containsKey(fullName)) {
                return reversedMap.get(fullName);
            }else {
                Utils.errPrintlnAndExit("Unknown resource type: %s", fullName);
                return null;
            }
        }

        public static Collection<String> fullNames() {
            return map.values();
        }
     }

    public static void setupByString(String jsonString) {
        jsonSpec = JsonSpec.ofString(jsonString);
        RES_ABBR.fullNames().forEach(s -> buildSempSpec("", s));

        sempSpecMap.put(BROKER_SPEC_PATH, brokerSpec());
        sempVersion = new SempVersion(jsonSpec.getSempVersionText());
        hardcodeSetup();
    }

    private static void hardcodeSetup() {
        HARD_CODE_REQUIRES_DISABLE.forEach((specPath, list) -> {
            var Requires_Disable = sempSpecMap.get(specPath).attributes.get(AttributeType.REQUIRES_DISABLE.toString());
            Requires_Disable.addAll(list);
        });
    }

    private static SempSpec brokerSpec() {
        var spec = new SempSpec();
        spec.attributes = new TreeMap<>();
        spec.defaultValues = new TreeMap<>();
        spec.childrenNames = new LinkedList<>(RES_ABBR.fullNames());
        spec.attributes.put(AttributeType.BROKER_SPECIFIC.toString(),
                List.of(SempSpec.SEMP_VERSION, SempSpec.OPAQUE_PASSWORD));
        spec.attributes.put(AttributeType.ALL.toString(),
                List.of(SempSpec.SEMP_VERSION, SempSpec.OPAQUE_PASSWORD));
        spec.attributeCombinations = Map.of();
        return spec;
    }

    private static void buildSempSpec(String parentObjectPath, String collectionName){
         var spec = SempSpec.of(parentObjectPath, collectionName);
        sempSpecMap.put(generateSpecPath( parentObjectPath + "/" + collectionName), spec);

        var objectPath = jsonSpec.getObjectPath(parentObjectPath + "/" + collectionName);
        spec.childrenNames.forEach(name -> buildSempSpec(objectPath, name));
    }

    private static SempSpec of(String parentObjectPath, String collectionName) {
        var spec = new SempSpec();
        var collectionPath = parentObjectPath + "/" + collectionName;
        var objectPath = jsonSpec.getObjectPath(collectionPath);
        jsonSpec.assertPathExist(collectionPath);

        spec.deprecated = jsonSpec.isDeprecatedCollection(collectionPath);
        spec.attributes = jsonSpec.findAttributes(collectionPath);
        spec.defaultValues = jsonSpec.getMapOfAttributesWithDefaultValue(collectionPath);
        spec.childrenNames = jsonSpec.getChildrenNames(objectPath);
        spec.sempClassName = jsonSpec.getSempClassName(collectionPath);
        spec.attributeCombinations = jsonSpec.findAttributesCombinations(collectionPath);
        spec.attributes.get(AttributeType.ALL.toString()).add(SempSpec.SKIP_THIS_OBJECT);

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
        if (RES_ABBR.fullNames().contains(resourceName)){
            return sempSpecMap.get("/"+resourceName).
                    getAttributeNames(AttributeType.IDENTIFYING).get(0);
        }else{
            throw new IllegalArgumentException(
                    String.format("%s is NOT one of [%s]!",
                            resourceName, RES_ABBR.fullNames()));
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

    public static String toPrettyString() {
        HashMap<String, Object> out = new HashMap<>();
        out.put(SEMP_VERSION, sempVersion.getText());
        out.put("sempSpecs", sempSpecMap);
        return Utils.toPrettyJsonMultiLineArray(out);
    }

    public Map<String, Object> getRequiresAttributeWithDefaultValue(Set<String> attributeNames) {
        var result = new HashMap<String, Object>();

        for (String attributeName : attributeNames) {
            var key = new AttributeCombinationKey(sempClassName, attributeName, AttributeCombinationKey.TYPE.Requires);
            Optional.ofNullable(attributeCombinations.get(key)).stream()
                    .flatMap(Collection::stream)
                    .filter(attr -> !attributeNames.contains(attr)) // is it already existed in the payload?
                    .filter(attr -> defaultValues.containsKey(attr)) // does it has default value?
                    .forEach(attr -> result.put(attr, defaultValues.get(attr)));
        }

        return result;
    }
}
