package semp.cfg.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import semp.cfg.Utils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JsonSpec {
    static final Logger logger = LoggerFactory.getLogger(JsonSpec.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private JsonNode root;
    private Object jsonDocument;
    private List<String> pathsList;

    public static JsonSpec ofJsonNode(JsonNode root){
        JsonSpec jsonSpec = new JsonSpec();
        jsonSpec.root = root;
        jsonSpec.pathsList = new LinkedList<>();
        root.get("paths").fieldNames().forEachRemaining(name -> jsonSpec.pathsList.add(name));

        String jsonString;
        try {
            jsonString = objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            jsonString = "";
        }
        jsonSpec.jsonDocument = Configuration.defaultConfiguration().jsonProvider().parse(jsonString);

        return jsonSpec;
    }

    protected boolean isPathExist(String specPath){
        return pathsList.contains(specPath);
    }

    protected void assertPathExist(String specPath){
        if (! isPathExist(specPath)){
            logger.error("Path '{}' is NOT found inside the SEMPv2 specification!", specPath);
            System.exit(1);
        }
    }

    protected boolean isDeprecatedCollection(String collectionPath){
        return Optional.of(root.get("paths").get(collectionPath).get("get"))
                .map(jsonNode -> jsonNode.get("deprecated"))
                .map(jsonNode -> jsonNode.asBoolean(false)).orElse(false);
    }

    protected String getObjectPath(String collectionPath) {
        var escapesPath = collectionPath
                .replace("{", "\\{")
                .replace("}", "\\}");
        var objRe = Pattern.compile("^" + escapesPath + "/([^/]+)$");
        return pathsList.stream()
                .filter(p -> objRe.matcher(p).matches())
                .findFirst()
                .orElseThrow();
    }

    protected static List<String> generateIdentifiers(String objectPath) {
        List<String> result = new LinkedList<>();
        var items = objectPath.split("/");
        var idsInPath = items[items.length-1];
        var m = Pattern.compile("\\{([^}]+)}").matcher(idsInPath);
        while (m.find()) {
            result.add(m.group(1));
        }
        return result;
    }

/*
Turn below description into a Map

Description:
```
    Update a Message VPN object. Any attribute missing from the request will be left unchanged.

    Message VPNs (Virtual Private Networks) allow for the segregation of topic space and clients. They also group clients connecting to a network of message brokers, such that messages published within a particular group are only visible to that group's clients.


    Attribute|Identifying|Read-Only|Write-Only|Requires-Disable|Deprecated|Opaque
    :---|:---:|:---:|:---:|:---:|:---:|:---:
    bridgingTlsServerCertEnforceTrustedCommonNameEnabled|||||x|
    msgVpnName|x|x||||
    replicationBridgeAuthenticationBasicPassword|||x|||x
    replicationBridgeAuthenticationClientCertContent|||x|||x
    replicationBridgeAuthenticationClientCertPassword|||x|||
    replicationEnabledQueueBehavior|||x|||
    restTlsServerCertEnforceTrustedCommonNameEnabled|||||x|

    ...
```

Map:
{
  "Read-Only" : [ "msgVpnName" ],
  "Requires-Disable" : [ ],
  "Deprecated" : [ "bridgingTlsServerCertEnforceTrustedCommonNameEnabled", "restTlsServerCertEnforceTrustedCommonNameEnabled" ],
  "Opaque" : [ "replicationBridgeAuthenticationBasicPassword", "replicationBridgeAuthenticationClientCertContent" ],
  "Identifying" : [ "msgVpnName" ],
  "Write-Only" : [ "replicationBridgeAuthenticationBasicPassword", "replicationBridgeAuthenticationClientCertContent", "replicationBridgeAuthenticationClientCertPassword", "replicationEnabledQueueBehavior" ]
}

 */
    protected Map<String, List<String>> findSpecialAttributes(String collectionPath){
        var description = getPatchOrPostDescription(collectionPath);
        var table = description.lines()
                .map(line -> line.split("\\|", -1))
                .filter(array -> array.length>=5)
                .map(Arrays::asList)
                .collect(Collectors.toList());

        var headers = table.get(0);
        var result = new HashMap<String, List<String>>();
        for (int i = 1; i < headers.size(); i++) { // start from second column
            var attributes = new LinkedList<String>();
            for (int j = 2; j < table.size(); j++) { // start from third row
                var x = table.get(j).get(i);
                if (x.equalsIgnoreCase("x")){
                    attributes.add(table.get(j).get(0)); // first column is the attribute name
                }
            }
            result.put(headers.get(i), attributes);
        }

        return result;
    }

    private String getPatchOrPostDescription(String collectionPath) {
        String objectPath = getObjectPath(collectionPath);
        var optionalS = Utils.jsonSafeGetValue(
                root, String.class, "paths", objectPath, "patch", "description");

        return optionalS.orElse(
                Utils.jsonSafeGetValue(root, String.class, "paths", collectionPath, "post", "description")
                        .orElse(""));
    }

    protected Map<String, ?> getMapOfAttributesWithDefaultValue(String collectionPath) {
        var propMap = getDefinitionProperties(collectionPath);
        return getAttributesWithDefaultValueFromJProperties(propMap);
    }

    protected Map<String, Map<String, ?>> getDefinitionProperties(String collectionPath){
        var conf = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS);
        var refPath = "$.paths." + collectionPath + ".post.parameters[?(@.name=='body')].schema.$ref";
        List<String> ref = JsonPath.using(conf).parse(jsonDocument).read(refPath);
        if (Objects.isNull(ref)){
            logger.warn("Unable to find path:{}", refPath);
            return null;
        }

        // "#/definitions/MsgVpn" -> "$.definitions.MsgVpn.properties"
        var propertiesPath = ref.get(0).replace("#", "$").replace("/", ".") + ".properties";
        Map<String, Map<String, ?>> result = JsonPath.using(conf).parse(jsonDocument).read(propertiesPath);
        if (Objects.isNull(result)){
            logger.warn("Unable to find path:{}", propertiesPath);
        }
        return result;
    }

    protected Map<String, ?> getAttributesWithDefaultValueFromJProperties(Map<String, Map<String, ?>> propMap) {
        Map<String, Object> result = new HashMap<>();
        propMap.forEach((attr, def)->{
            if (!def.containsKey("description")) return;
            String description = (String) def.get("description");
            var defaultValue = findDefaultValue(description);
            if (defaultValue.isEmpty()) return;

            String value = defaultValue.get();
            var type = (String) def.get("type");
            // remove quote marks at both the begin and the end
            switch (type) {
                case "string" -> result.put(attr, value.substring(1, value.length() - 1));
                case "integer" -> result.put(attr, Integer.parseInt(value));
                case "boolean" -> result.put(attr, Boolean.parseBoolean(value));
                default -> logger.error("Unknown type '{}' of property '{}', the default value is {}",
                        type, attr, value);
            }
        });

        return result;
    }

    protected static Optional<String> findDefaultValue(String description) {
        var re = Pattern.compile("The default value is `([^`]+)`");
        var m = re.matcher(description);
        if (m.find()) {
            return Optional.of(m.group(1));
        }else {
            return Optional.empty();
        }
    }
}
