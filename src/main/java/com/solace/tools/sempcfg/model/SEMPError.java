package com.solace.tools.sempcfg.model;

import java.util.HashMap;
import java.util.Map;

public enum SEMPError {
    NOT_FOUND(6),
    ALREADY_EXISTS(10),
    NOT_ALLOWED(89),
    CONFIGDB_OBJECT_DEPENDENCY(490),
    ;

    private static Map<Integer, SEMPError> map = new HashMap<>();
    static {
        map.put(6, NOT_FOUND);
        map.put(10, ALREADY_EXISTS);
        map.put(89, NOT_ALLOWED);
        map.put(490, CONFIGDB_OBJECT_DEPENDENCY);
    }
    private int value;

    SEMPError(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static SEMPError ofInt(int value) {
        return map.get(value);
    }
}
