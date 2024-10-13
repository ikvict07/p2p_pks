package sk.stuba.pks.old.model;

public enum PayloadType {
    KEEP_ALIVE("keep-alive"),
    DATA("data");

    public final String type;

    private PayloadType(String type) {
        this.type = type;
    }
}
