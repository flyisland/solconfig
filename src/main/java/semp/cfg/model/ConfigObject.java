package semp.cfg.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.SneakyThrows;
import semp.cfg.RestCommandList;
import semp.cfg.Utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigObject {
    private final String collectionName;
    protected TreeMap<String, Object> attributes;
    @Getter
    private final TreeMap<String, List<ConfigObject>> children;
    private String specPath;
    private SempSpec sempSpec;
    private String collectionPath;
    private String objectPath="";

    public ConfigObject(){
        this(null);
    }

    private ConfigObject(String collectionName){
        this.collectionName = collectionName;
        attributes = new TreeMap<>();
        children = new TreeMap<>();
    }

    public ConfigObject addChild(String collectionName, Map<String, Object> attributes) {
        ConfigObject child = new ConfigObject(collectionName);
        child.setSpecPath(specPath + "/" + child.collectionName);
        for (String name : child.sempSpec.getAttributeNames(AttributeType.ALL)) {
            Optional.ofNullable(attributes.get(name))
                    .ifPresent(v -> child.attributes.put(name, v));
        }
        child.collectionPath = objectPath + "/" + collectionName;
        child.objectPath = child.collectionPath + "/" + child.getObjectId();
        children.computeIfAbsent(child.collectionName, k -> new LinkedList<>()).add(child);
        return child;
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
        var idList = sempSpec.getAttributeNames(AttributeType.IDENTIFYING).stream()
                .map(id -> attributes.get(id).toString())
                .map(s -> URLEncoder.encode(s, StandardCharsets.UTF_8))
                .collect(Collectors.toList());
        return String.join(",", idList);
    }

    /**
     * Remove all children objects if it meets the filter's test
     * @param filters  a predicate which returns true for objects to be removed
     */
    @SafeVarargs
    public final void removeChildrenObjects(Predicate<ConfigObject>... filters) {
        for (Iterator<List<ConfigObject>> iterator = children.values().iterator(); iterator.hasNext(); ) {
            var list =  iterator.next();
            for (Predicate<ConfigObject> filter : filters) {
                list.removeIf(filter);
            }
            if (list.isEmpty()) {
                iterator.remove();
            }
        }
        children.values().forEach(list -> list.forEach(configObject -> configObject.removeChildrenObjects(filters)));
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
     * @param types of attributes to remove
     */
    public void removeAttributes(AttributeType ... types) {
        for (AttributeType type : types) {
            var attributesToRemove = sempSpec.getAttributeNames(type);
            attributesToRemove.forEach(attrName -> attributes.remove(attrName));
        }
        children.values().forEach(list -> list.forEach(configObject -> configObject.removeAttributes(types)));
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

    public void forEachChild(Consumer<ConfigObject> action) {
        getChildren().values().stream()
                .flatMap(Collection::stream)
                .forEach(action);
    }

    public void generateDeleteCommands(RestCommandList commandList) {
        var requiresDisable = ifRequiresDisableBeforeChangeChildren();
        if(requiresDisable) {
            commandList.append(HTTPMethod.PATCH, objectPath, String.format("{\"%s\":%b}",
                    SempSpec.ENABLED_ATTRIBUTE_NAME, false));
        }

        forEachChild(configObject -> configObject.generateDeleteCommands(commandList));

        if (isDefaultObject()){
            if(sempSpec.getAttributeNames(AttributeType.ALL).contains("enabled")) {
                commandList.append(HTTPMethod.PATCH, objectPath, "{\"enabled\":false}");
            }
        }else {
            commandList.append(HTTPMethod.DELETE, objectPath, null);
        }
    }

    public void addChildrenFromMap(Map<String, Object> input) {
        var childrenNames = sempSpec.getChildrenNames();
        childrenNames.forEach(childName->{
            Optional.ofNullable(input.get(childName)).ifPresent(list->{
                var childrenList = (List<Map<String, Object>>) list;
                childrenList.forEach( childMap -> {
                    var child = addChild(childName, childMap);
                    child.addChildrenFromMap(childMap);
                });
            });
        });

    }

    public void generateCreateCommands(RestCommandList commandList) {
        var requiresDisable = ifRequiresDisableBeforeChangeChildren();
        if (requiresDisable) {
            attributes.put(SempSpec.ENABLED_ATTRIBUTE_NAME, false);
        }
        var payload = toJsonStringAttributeOnly();
        if (isDefaultObject()) {
            commandList.append(HTTPMethod.PATCH, objectPath, payload);
        } else {
            commandList.append(HTTPMethod.POST, collectionPath, payload);
        }

        forEachChild(configObject -> configObject.generateCreateCommands(commandList));

        if(requiresDisable) {
            commandList.append(HTTPMethod.PATCH, objectPath, String.format("{\"%s\":%b}",
                    SempSpec.ENABLED_ATTRIBUTE_NAME, true));
        }
    }

    private boolean ifRequiresDisableBeforeChangeChildren() {
        if (! Optional.ofNullable((Boolean) attributes.get(SempSpec.ENABLED_ATTRIBUTE_NAME))
                .orElse(false)){
            return false;
        }
        for (String s : children.keySet()) {
            if (SempSpec.SPEC_PATHS_OF_REQUIRES_DISABLE_CHILD.contains(specPath+"/"+s)) {
                return true;
            }
        }
        return false;
    }

    private boolean ifRequiresDisableBeforeUpdateAttributes(ConfigObject objNew) {
        if (! Optional.ofNullable((Boolean) objNew.attributes.get(SempSpec.ENABLED_ATTRIBUTE_NAME))
                .orElse(false)){
            return false;
        }
        var mergeSet = Utils.symmetricDiff(attributes.entrySet(), objNew.attributes.entrySet());
        var Requires_Disable = sempSpec.getAttributeNames(AttributeType.REQUIRES_DISABLE);
        return mergeSet.stream().anyMatch(e -> Requires_Disable.contains(e.getKey()));
    }

    private boolean ifRequiresDisableBeforeChangeChildren(ConfigObject objNew) {
        if (! Optional.ofNullable((Boolean) objNew.attributes.get(SempSpec.ENABLED_ATTRIBUTE_NAME))
                .orElse(false)){
            return false;
        }

        return Stream.concat(children.keySet().stream(), objNew.children.keySet().stream())
                .distinct()
                .map(childCollectionName -> specPath + "/" + childCollectionName)
                .anyMatch(SempSpec.SPEC_PATHS_OF_REQUIRES_DISABLE_CHILD::contains);
    }


    public void generateObjectUpdateCommands(ConfigObject newObj, RestCommandList updateCommandList) {
        var requiresDisable = ifRequiresDisableBeforeUpdateAttributes(newObj) ||
                ifRequiresDisableBeforeChangeChildren(newObj);
        if (requiresDisable) {
            newObj.attributes.put(SempSpec.ENABLED_ATTRIBUTE_NAME, false);
        }

        var payload = newObj.toJsonStringAttributeOnly();
        updateCommandList.append(HTTPMethod.PUT, objectPath, payload);

        // TODO: update children

        if(requiresDisable) {
            updateCommandList.append(HTTPMethod.PATCH, objectPath, String.format("{\"%s\":%b}",
                    SempSpec.ENABLED_ATTRIBUTE_NAME, true));
        }
    }

}
