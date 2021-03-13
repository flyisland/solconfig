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
        SempSpec.setupByString(sempClient.sendWithResourcePath("get", "/spec", null));
    }

    public void backup(String resourceType, String[] objectNames){
        exitOnObjectsNotExist(resourceType, objectNames);
        ConfigBroker configBroker = getConfigBroker(resourceType, objectNames);

        configBroker.removeChildrenObjects(ConfigObject::isReservedObject);
        configBroker.removeChildrenObjects(ConfigObject::isDeprecatedObject);
        configBroker.removeAttributes(AttributeType.PARENT_IDENTIFIERS);
        configBroker.removeAttributes(AttributeType.DEPRECATED);
        configBroker.removeAttributesWithDefaultValue();
        System.out.println(configBroker.toString());
    }

    public void delete(String resourceType, String[] objectNames) {
        exitOnObjectsNotExist(resourceType, objectNames);
        ConfigBroker configBroker = getConfigBroker(resourceType, objectNames);
        configBroker.removeChildrenObjects(ConfigObject::isReservedObject);
        configBroker.removeChildrenObjects(ConfigObject::isDeprecatedObject);
        configBroker.removeChildrenObjects(ConfigObject::isDefaultObject);

        var commandList = new RestCommandList();
        configBroker.getChildren().values().forEach(
                list -> list.forEach(
                        configObject -> configObject.generateDeleteCommands(commandList, "")));
        commandList.execute(sempClient, curlOnly);
    }

    private ConfigBroker getConfigBroker(String resourceType, String[] objectNames) {
        ConfigBroker configBroker = new ConfigBroker();
        configBroker.setSempVersion(SempSpec.getSempVersion());

        var nameList = Arrays.asList(objectNames);
        if (nameList.contains("*")){
            nameList = List.of("*");
        }
        Map<String, String> childrenLinks = nameList.stream()
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
                var sempResponse = sempClient.getCollectionWithAbsoluteUri(e.getValue());
                if (sempResponse.isEmpty()){
                    return;
                }

                List<Map<String, Object>> data = sempResponse.getData();
                List<Map<String, String>> links = sempResponse.getLinks();
                for (int i = 0; i < data.size(); i++) {
                    ConfigObject child = ConfigObject.ofAttributes(collectionName, data.get(i));
                    configObject.addChild(child);
                    getChildrenRecursively(child, links.get(i));
                }
            });
    }

    public void restore(File confFile) {
        ConfigBroker configBroker = new ConfigBroker();
        configBroker.fromMap(SempClient.readMapFromJsonFile(confFile));
        if (!curlOnly){
            exitOnObjectsAlreadyExist(configBroker);
        }

        var commandList = new RestCommandList();
        configBroker.getChildren().values().forEach(
                list -> list.forEach(
                        configObject -> configObject.generatRestoreCommands(commandList, "")));
        commandList.execute(sempClient, curlOnly);
    }

    private void exitOnObjectsNotExist(String resourceType, String[] objectNames) {
        checkObjectsExistence(resourceType, Arrays.asList(objectNames), false);
    }

    private void exitOnObjectsAlreadyExist(ConfigBroker configBroker) {
        var children=configBroker.getChildren().entrySet().stream().findFirst();
        if (children.isEmpty()) {
            Utils.errPrintlnAndExit("There is no objects to restore!");
        }else {
            var nameList = children.get().getValue().stream()
                    .map(ConfigObject::getObjectId)
                    .collect(Collectors.toList());
            checkObjectsExistence(children.get().getKey(), nameList, true);
        }
    }

    private void checkObjectsExistence(String resourceType, List<String> objectNames, boolean existOn) {
        var objects = sempClient.checkIfObjectsExist(resourceType, objectNames);
        var resultSet = objects.stream()
                .filter(e -> e.getValue() == existOn)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        if (resultSet.isEmpty()) {
            return;
        }
        var type = SempSpec.TOP_RESOURCES.entrySet().stream()
                .filter(e -> e.getValue().equals(resourceType))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("");
        Utils.errPrintlnAndExit((Exception) null, "Resource %s[%s] %s exist!",
                type,
                String.join(", ", resultSet),
                existOn ? "already" : "doesn't");
    }

}
