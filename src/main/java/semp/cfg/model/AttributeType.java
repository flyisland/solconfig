package semp.cfg.model;

public enum AttributeType {
    ALL("ALL"),
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
