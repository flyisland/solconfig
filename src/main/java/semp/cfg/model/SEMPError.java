package semp.cfg.model;

public enum SEMPError {
    NOT_ALLOWED(89),
    ALREADY_EXISTS(10);

    private int value;

    SEMPError(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
