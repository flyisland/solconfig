package semp.cfg.model;

public enum HTTPMethod {
    POST,
    PATCH,
    PUT,
    DELETE;

    public String toSEMPMethod() {
        return name().toLowerCase();
    }
}
