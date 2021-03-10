package semp.cfg.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class JsonSpec {
    static final Logger logger = LoggerFactory.getLogger(JsonSpec.class);
    private JsonNode root;
    private List<String> pathsList;

    public static JsonSpec ofJsonNode(JsonNode root){
        JsonSpec jsonSpec = new JsonSpec();
        jsonSpec.root = root;
        jsonSpec.pathsList = new LinkedList<>();
        root.get("paths").fieldNames().forEachRemaining(name -> jsonSpec.pathsList.add(name));
        return jsonSpec;
    }

    protected boolean isPathExist(String specPath){
        return pathsList.contains(specPath);
    }

    protected void assertPathExist(String specPath){
        if (! isPathExist(specPath)){
            logger.error("Path '{}' is NOT found inside the SEMP specification!", specPath);
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
}
