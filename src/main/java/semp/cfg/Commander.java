package semp.cfg;

import lombok.Setter;
import semp.cfg.model.ConfigBroker;
import semp.cfg.model.ConfigObject;
import semp.cfg.model.SempResponse;

import java.util.*;
import java.util.stream.Collectors;

public class Commander {
    @Setter
    private SempClient sempClient;
    @Setter
    private boolean curlOnly;

    public void backup(String resourceType, String[] objectNames){
        ConfigBroker configBroker = new ConfigBroker();
        configBroker.setSempVersion(sempClient.getSempVersion());

        Map<String, String> childrenLinks = Arrays.stream(objectNames)
                .map(objectName -> Map.entry(objectName,
                        sempClient.buildAbsoluteUri(
                                String.format("/%s?where=msgVpnName==%s", resourceType, objectName))))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        getChildrenRecursively(configBroker, childrenLinks);
        System.out.println(configBroker.toString());
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
                    ConfigObject child = ConfigObject.ofProperties(collectionName, data.get(i));
                    configObject.addChild(child);
                    getChildrenRecursively(child, links.get(i));
                }
            });
    }
}
