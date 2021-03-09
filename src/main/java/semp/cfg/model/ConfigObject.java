package semp.cfg.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.*;
import java.util.stream.Collectors;

public class ConfigObject {
    private String collectionName;
    protected Map<String, Object> properties;
    protected Map<String, List<ConfigObject>> children;
    static private ObjectMapper objectMapper = new ObjectMapper();

    public ConfigObject(){
        this(null);
    }

    private ConfigObject(String collectionName){
        this.collectionName = collectionName;
        properties = new HashMap<>();
        children = new HashMap<>();
    }

    public static ConfigObject ofProperties(String collectionName, Map<String, Object> properties){
        ConfigObject configObject = new ConfigObject(collectionName);
        configObject.properties = properties;
        return configObject;
    }

    public void addChild(ConfigObject configObject){
        children.computeIfAbsent(configObject.collectionName, k -> new LinkedList<>()).add(configObject);
    }

    protected Map<String, ?> toMap(){
        Map<String, Object> result = new TreeMap<>(properties);
        children.forEach((k, l)->{
            List<Map<String, ?>> childrenList = l.stream()
                .map(ConfigObject::toMap)
                .collect(Collectors.toList());
            result.put(k, childrenList);
        });
        return result;
    }

    @SneakyThrows
    @Override
    public String toString() {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(toMap());
    }
}
