package semp.cfg.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.util.*;

public class ConfigObject {
    private final String collectionName;
    protected TreeMap<String, Object> attributes;
    private String specPath;
    private TreeMap<String, List<ConfigObject>> children;
    static private ObjectMapper mapper = new ObjectMapper();

    public ConfigObject(){
        this(null);
    }

    private ConfigObject(String collectionName){
        this.collectionName = collectionName;
        specPath = "";
        attributes = new TreeMap<>();
        children = new TreeMap<>();
    }

    public static ConfigObject ofAttributes(String collectionName, Map<String, Object> attributes){
        ConfigObject configObject = new ConfigObject(collectionName);
        configObject.attributes = new TreeMap<>(attributes);
        return configObject;
    }

    public void addChild(ConfigObject child){
        child.specPath = specPath +"/"+child.collectionName;
        children.computeIfAbsent(child.collectionName, k -> new LinkedList<>()).add(child);
    }

    private boolean hasChildren(){
        if (Objects.isNull(children)){
            return false;
        }
        return !children.isEmpty();
    }

    private static final String TAB_SPACE=" ".repeat(2);
    private StringBuilder toJsonString(int level) throws JsonProcessingException {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s{%n", TAB_SPACE.repeat(level)));
        sb.append(attributesToJsonString(level+1));
        sb.append(childrenToJsonString(level+1));
        sb.append(String.format("%s}", TAB_SPACE.repeat(level)));
        return sb;
    }

    private StringBuilder attributesToJsonString(int level) throws JsonProcessingException {
        StringBuilder sb = new StringBuilder();
        Iterator<String > names = attributes.navigableKeySet().iterator();
        while (names.hasNext()){
            String name = names.next();
            sb.append(String.format(
                    "%s%s: %s%s%n",
                    TAB_SPACE.repeat(level),
                    mapper.writeValueAsString(name),
                    mapper.writeValueAsString(attributes.get(name)),
                    names.hasNext() || hasChildren() ?",":""
            ));
        }
        return sb;
    }

    private StringBuilder childrenToJsonString(int level) throws JsonProcessingException {
        StringBuilder sb = new StringBuilder();
        Iterator<String > names = children.navigableKeySet().iterator();
        while (names.hasNext()){
            String name = names.next();
            sb.append(String.format("%s%s: [%n", TAB_SPACE.repeat(level), mapper.writeValueAsString(name)));
            Iterator<ConfigObject> list = children.get(name).iterator();
            while (list.hasNext()){
                sb.append(list.next().toJsonString(level + 1));
                sb.append(String.format("%s%n", list.hasNext()?",":""));
            }
            sb.append(String.format("%s]%s%n", TAB_SPACE.repeat(level), names.hasNext()?",":""));
        }
        return sb;
    }


    @SneakyThrows
    @Override
    public String toString() {
        return toJsonString(0).toString();
    }
}
