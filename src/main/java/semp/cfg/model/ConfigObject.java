package semp.cfg.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.SneakyThrows;
import semp.cfg.RestCommandList;
import semp.cfg.Utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ConfigObject {
    private final String collectionName;
    protected TreeMap<String, Object> attributes;
    @Getter
    private final TreeMap<String, List<ConfigObject>> children;
    private String specPath;
    private SempSpec sempSpec;

    public ConfigObject(){
        this(null);
    }

    private ConfigObject(String collectionName){
        this.collectionName = collectionName;
        attributes = new TreeMap<>();
        children = new TreeMap<>();
    }

    public static ConfigObject ofAttributes(String collectionName, Map<String, Object> attributes){
        ConfigObject configObject = new ConfigObject(collectionName);
        configObject.attributes = new TreeMap<>(attributes);
        return configObject;
    }

    public void addChild(ConfigObject child){
        child.setSpecPath(specPath + "/" + child.collectionName);
        children.computeIfAbsent(child.collectionName, k -> new LinkedList<>()).add(child);
    }

    protected void setSpecPath(String path) {
        this.specPath = path;
        this.sempSpec = SempSpec.get(specPath);
    }

    private boolean hasChildren(){
        if (Objects.isNull(children)){
            return false;
        }
        return !children.isEmpty();
    }

    private static final String TAB_SPACE=" ".repeat(2);
    private StringBuilder toJsonString() {
        return toJsonString(0, false);
    }

    private String toJsonStringAttributeOnly(){
        return toJsonString(0, true).toString();
    }

    private StringBuilder toJsonString(int level, boolean attributeOnly){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s{%n", TAB_SPACE.repeat(level)));
        sb.append(attributesToJsonString(level+1, attributeOnly));
        if (!attributeOnly) {
            sb.append(childrenToJsonString(level+1));
        }
        sb.append(String.format("%s}", TAB_SPACE.repeat(level)));
        return sb;
    }

    private StringBuilder attributesToJsonString(int level, boolean attributeOnly) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> names = attributes.navigableKeySet().iterator();
        while (names.hasNext()) {
            String name = names.next();
            try {
                sb.append(String.format(
                        "%s%s: %s",
                        TAB_SPACE.repeat(level),
                        Utils.objectMapper.writeValueAsString(name),
                        Utils.objectMapper.writeValueAsString(attributes.get(name))));
            } catch (JsonProcessingException e) {
                Utils.errPrintlnAndExit(e, "Unable to convert %s: %s to JSON format.",
                        name, attributes.get(name));
            }
            if (names.hasNext()) {
                sb.append(",");
            } else if (!attributeOnly && hasChildren()) {
                sb.append(",");
            }
            sb.append("\n");
        }
        return sb;
    }


    private StringBuilder childrenToJsonString(int level) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> names = children.navigableKeySet().iterator();
        while (names.hasNext()) {
            String name = names.next();
            try {
                sb.append(String.format("%s%s: [%n", TAB_SPACE.repeat(level), Utils.objectMapper.writeValueAsString(name)));
            } catch (JsonProcessingException e) {
                Utils.errPrintlnAndExit(e, "Unable to convert %s to JSON format.%n", name);
            }
            Iterator<ConfigObject> list = children.get(name).iterator();
            while (list.hasNext()) {
                sb.append(list.next().toJsonString(level + 1, false));
                sb.append(String.format("%s%n", list.hasNext() ? "," : ""));
            }
            sb.append(String.format("%s]%s%n", TAB_SPACE.repeat(level), names.hasNext() ? "," : ""));
        }
        return sb;
    }

    /**
     *  /msgVpns/{msgVpnName}/aclProfiles/{aclProfileName}/publishTopicExceptions/{publishTopicExceptionSyntax},{publishTopicException}
     *  Obj-id must be url encoded and join with "," as above example.
     * @return the obj-id
     */
    public String getObjectId() {
        var idList = sempSpec.getSpecialAttributes(AttributeType.IDENTIFYING).stream()
                .map(id -> attributes.get(id).toString())
                .map(s -> URLEncoder.encode(s, StandardCharsets.UTF_8))
                .collect(Collectors.toList());
        return String.join(",", idList);
    }

    /**
     * Remove all children objects if it meets the filter's test
     * @param filter  a predicate which returns true for objects to be removed
     */
    public void removeChildrenObjects(Predicate<ConfigObject> filter) {
        for (Iterator<List<ConfigObject>> iterator = children.values().iterator(); iterator.hasNext(); ) {
            var list =  iterator.next();
            list.removeIf(filter);
            if (list.isEmpty()) {
                iterator.remove();
            }
        }
        children.values().forEach(list -> list.forEach(configObject -> configObject.removeChildrenObjects(filter)));
    }

    public boolean isDeprecatedObject() {
        return sempSpec.isDeprecated();
    }

    /**
     * Names starting with '#' are reserved. Reserved object can not be created or delete by users.
     * %23 is the url encoded '#'
     *
     * @return if this object is a reserved object.
     */
    public boolean isReservedObject() {
        return getObjectId().startsWith("%23");
    }

    public boolean isDefaultObject() {
        return getObjectId().equals(SempSpec.DEFAULT_OBJECT_NAME) &&
                SempSpec.SPEC_PATHS_OF_DEFAULT_OBJECT.contains(specPath);
    }

    /**
     * Removes 'type' of attributes of all objects
     * @param type of attributes to remove
     */
    public void removeAttributes(AttributeType type) {
        var attributesToRemove = sempSpec.getSpecialAttributes(type);
        attributesToRemove.forEach(attrName -> attributes.remove(attrName));
        children.values().forEach(list -> list.forEach(configObject -> configObject.removeAttributes(type)));
    }

    public void removeAttributesWithDefaultValue() {
        attributes.entrySet().removeAll(sempSpec.getDefaultValues().entrySet());
        children.values().forEach(list -> list.forEach(ConfigObject::removeAttributesWithDefaultValue));
    }

    @SneakyThrows
    @Override
    public String toString() {
        return toJsonString().toString();
    }

    public void generateDeleteCommands(RestCommandList commandList, String parentPath) {
        var objectPath = String.format("%s/%s/%s",
                parentPath, collectionName, getObjectId());
        children.values().forEach(
                list -> list.forEach(
                        configObject -> configObject.generateDeleteCommands(commandList, objectPath)));

        commandList.append(HTTPMethod.DELETE, objectPath, null);
    }

    public void fromMap(Map<String, Object> input) {
        var childrenNames = sempSpec.getChildrenNames();
        input.forEach((key, value)->{
            if (childrenNames.contains(key)) {
                var childrenList = (List<Map<String, Object>>) value;
                childrenList.forEach( childMap -> {
                    var child = new ConfigObject(key);
                    addChild(child);
                    child.fromMap(childMap);
                });
            } else {
                attributes.put(key, value);
            }
        });
    }

    public void generatRestoreCommands(RestCommandList commandList, String parentPath) {
        var collectionPath = parentPath + "/" + collectionName;
        var objectPath = collectionPath + "/" + getObjectId();
        var payload = toJsonStringAttributeOnly();

        if (isDefaultObject()) {
            commandList.append(HTTPMethod.PATCH, objectPath, payload);
        } else {
            commandList.append(HTTPMethod.POST, collectionPath, payload);
        }

        children.values().forEach(
                list -> list.forEach(
                        configObject -> configObject.generatRestoreCommands(commandList, objectPath)));

    }
}
