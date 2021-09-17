package com.solace.tools.solconfig.model;

import com.solace.tools.solconfig.Utils;
import lombok.Getter;

import java.util.Objects;

public class SempVersion implements Comparable<SempVersion>{
    @Getter
    private String text;
    private int   number;

    public SempVersion(String version) {
        if (Objects.isNull(version)){
            Utils.errPrintlnAndExit(new IllegalArgumentException("Null is an illegal SEMPv2 version."),"Unable to new a SempVersion object");
        }
        this.text = version;
        String[] v = version.split("\\.");

        // both "2.22" and "2.11.00091010036" are legal version, check https://github.com/flyisland/solconfig/issues/3
        if (v.length < 2) {
            Utils.errPrintlnAndExit(new IllegalArgumentException(version+" is an illegal SEMPv2 version."),"Unable to new a SempVersion object");
        }
        try {
            number = Integer.parseInt(v[0]) * 1000 + Integer.parseInt(v[1]);
        }catch (NumberFormatException e){
            Utils.errPrintlnAndExit(new IllegalArgumentException(version+" is an illegal SEMPv2 version."),"Unable to new a SempVersion object");
        }
    }

    @Override
    public String toString() {
        return "SempVersion{" +
                "text='" + text + '\'' +
                ", number=" + number +
                '}';
    }

    @Override
    public int compareTo(SempVersion input) {
        return number-input.number;
    }
}
