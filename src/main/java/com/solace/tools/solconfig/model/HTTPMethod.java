package com.solace.tools.solconfig.model;

public enum HTTPMethod {
    GET,
    POST,
    PATCH,
    PUT,
    DELETE;

    public String toSEMPMethod() {
        return name().toLowerCase();
    }
}
