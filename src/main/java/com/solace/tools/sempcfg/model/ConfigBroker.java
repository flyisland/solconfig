package com.solace.tools.sempcfg.model;


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

    public String getOpaquePassword() {
        return (String) attributes.get(SempSpec.OPAQUE_PASSWORD);
    }

    public SempVersion getSempVersion(){
        return (SempVersion) attributes.get(SempSpec.SEMP_VERSION);
    }
}
