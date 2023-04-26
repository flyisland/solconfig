package com.solace.tools.solconfig.model;

public class SolConfigException extends RuntimeException {
    public SolConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public SolConfigException(String message) {
        super(message);
    }
}
