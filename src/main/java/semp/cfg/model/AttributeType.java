package semp.cfg.model;

public enum AttributeType {
    PARENT_IDENTIFIERS("Parent-Identifiers"),
    IDENTIFYING("Identifying"),
    REQUIRED("Required"),
    DEPRECATED("Deprecated"),
    ;

    private final String type;

    AttributeType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
