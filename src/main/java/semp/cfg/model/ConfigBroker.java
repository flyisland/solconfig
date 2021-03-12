package semp.cfg.model;


public class ConfigBroker extends ConfigObject{

    public ConfigBroker(){
        super();
        setSpecPath(SempSpec.BROKER_SPEC_PATH);
    }

    public void setSempVersion(SempVersion sempVersion){
        attributes.put(SempSpec.SEMP_VERSION, sempVersion.getText());
    }
}
