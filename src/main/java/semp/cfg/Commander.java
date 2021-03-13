package semp.cfg;

import lombok.Setter;
import semp.cfg.model.*;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Commander {
    private SempClient sempClient;
    @Setter
    private boolean curlOnly;

    public static Commander ofSempClient(SempClient sempClient){
        Commander commander = new Commander();
        commander.sempClient = sempClient;
        commander.setupSempSpec();
        return commander;
    }

    private void setupSempSpec() {
        SempSpec.setup(sempClient.sendWithResourcePath("get", "/spec", null));
    }

    public void backup(String resourceType, String[] objectNames){
        ConfigBroker configBroker = generateConfigBroker(resourceType, objectNames);
        configBroker.removeChildrenObjects(ConfigObject::isReservedObject);
        configBroker.removeChildrenObjects(ConfigObject::isDeprecatedObject);
        configBroker.removeAttributes(AttributeType.PARENT_IDENTIFIERS);
        configBroker.removeAttributes(AttributeType.DEPRECATED);
        configBroker.removeAttributesWithDefaultValue();
        System.out.println(configBroker.toString());
    }

    public void delete(String resourceType, String[] objectNames) {
        ConfigBroker configBroker = generateConfigBroker(resourceType, objectNames);
        configBroker.removeChildrenObjects(ConfigObject::isReservedObject);
        configBroker.removeChildrenObjects(ConfigObject::isDeprecatedObject);
        configBroker.removeChildrenObjects(ConfigObject::isDefaultObject);

        var commandList = new RestCommandList();
        configBroker.getChildren().values().forEach(
                list -> list.forEach(
                        configObject -> configObject.generateDeleteCommands(commandList, "")));
        if (curlOnly) {
            System.err.println(commandList);
        } else {
            commandList.execute(sempClient);
        }
    }

    private ConfigBroker generateConfigBroker(String resourceType, String[] objectNames) {
        ConfigBroker configBroker = new ConfigBroker();
        configBroker.setSempVersion(SempSpec.getSempVersion());

        Map<String, String> childrenLinks = Arrays.stream(objectNames)
                .map(objectName -> Map.entry(objectName,
                        sempClient.buildAbsoluteUri(
                                String.format("/%s?where=%s==%s",
                                        resourceType, SempSpec.getTopResourceIdentifierKey(resourceType), objectName))))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        getChildrenRecursively(configBroker, childrenLinks);
        return configBroker;
    }

    private void getChildrenRecursively(ConfigObject configObject, Map<String, String> childrenLinks){
        childrenLinks.entrySet().stream()
            .filter(e -> !e.getKey().equals("uri"))
            .forEach(e -> {
                String collectionName = Utils.getCollectionNameFromUri(e.getValue());
                Optional<SempResponse> sempResponse = sempClient.getCollectionWithAbsoluteUri(e.getValue());
                if (sempResponse.isEmpty() || sempResponse.get().isEmpty()){
                    return;
                }

                List<Map<String, Object>> data = sempResponse.get().getData();
                List<Map<String, String>> links = sempResponse.get().getLinks();
                for (int i = 0; i < data.size(); i++) {
                    ConfigObject child = ConfigObject.ofAttributes(collectionName, data.get(i));
                    configObject.addChild(child);
                    getChildrenRecursively(child, links.get(i));
                }
            });
    }

    public void restore(File confFile) {
        var confMap = SempClient.readMapFromJsonFile(confFile);
        ConfigBroker configBroker = new ConfigBroker();
        configBroker.fromMap(confMap);

        var commandList = new RestCommandList();
        configBroker.getChildren().values().forEach(
                list -> list.forEach(
                        configObject -> configObject.generatRestoreCommands(commandList, "")));
        if (curlOnly) {
            System.err.println(commandList);
        } else {
            commandList.execute(sempClient);
        }
    }
}
