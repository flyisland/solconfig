package semp.cfg.model;

import lombok.Getter;

import java.util.Objects;

public class SempVersion{
    @Getter
    private String text;
    private int   number;

    public SempVersion(String version) throws IllegalArgumentException {
        if (Objects.isNull(version)){
            throw new IllegalArgumentException("Null is an illegal SEMPv2 version.");
        }
        this.text = version;
        String[] v = version.split("\\.");
        if (v.length !=2) {
            throw new IllegalArgumentException(version+" is an illegal SEMPv2 version.");
        }
        try {
            number = Integer.parseInt(v[0]) * 1000 + Integer.parseInt(v[1]);
        }catch (NumberFormatException e){
            throw new IllegalArgumentException(version+" is an illegal SEMPv2 version.");
        }
    }

    @Override
    public String toString() {
        return "SempVersion{" +
                "text='" + text + '\'' +
                ", number=" + number +
                '}';
    }
}
