package semp.cfg.model;

public enum SEMPError {
    NOT_FOUND(6),
    ALREADY_EXISTS(10),
    NOT_ALLOWED(89),
    ;

    private int value;

    SEMPError(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
