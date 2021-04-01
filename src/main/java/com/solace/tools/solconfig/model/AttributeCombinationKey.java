package com.solace.tools.solconfig.model;

import java.util.Objects;

public class AttributeCombinationKey implements Comparable<AttributeCombinationKey>{
    private String sempClassName;
    private String attributeName;
    private TYPE type;

    public enum TYPE {
        Requires, Conflicts
    }

    public AttributeCombinationKey(String sempClassName, String attributeName, TYPE type) {
        this.sempClassName = sempClassName;
        this.attributeName = attributeName;
        this.type = type;
    }

    @Override
    public String toString() {
        return "AttributeCombinationKey{" +
                "sempClassName='" + sempClassName + '\'' +
                ", attributeName='" + attributeName + '\'' +
                ", type=" + type +
                '}';
    }

    @Override
    public int compareTo(AttributeCombinationKey o) {
        if (Objects.isNull(o)) return 1;
        return sempClassName.concat(attributeName).concat(type.toString())
                .compareTo(o.sempClassName.concat(o.attributeName).concat(o.type.toString()));
    }

}
