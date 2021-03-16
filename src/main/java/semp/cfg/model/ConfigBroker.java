package semp.cfg.model;


import java.util.Optional;

public class ConfigBroker extends ConfigObject{

    public ConfigBroker(){
        super();
        setSpecPath(SempSpec.BROKER_SPEC_PATH);
    }

    public void setSempVersion(SempVersion sempVersion){
        attributes.put(SempSpec.SEMP_VERSION, sempVersion.getText());
    }

    public void setOpaquePassword(String opaquePassword) {
        if (Optional.ofNullable(opaquePassword).map(String::isEmpty).orElse(true)) {
            return;
        }
        attributes.put(SempSpec.OPAQUE_PASSWORD, opaquePassword);
    }
}
