package semp.cfg.model;


public class ConfigBroker extends ConfigObject{

    public ConfigBroker(){
        super();
    }

    public void setSempVersion(SempVersion sempVersion){
        attributes.put(SempSpec.SEMP_VERSION, sempVersion.getText());
    }
}
