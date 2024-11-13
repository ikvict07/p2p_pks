package sk.stuba.pks.library.model;

public enum MessageType {
    SIMPLE("simple"),
    FILE("file"),
    SYN("syn");

    public final String type;
    public static MessageType fromString(String type) {
        return switch (type) {
            case "simple" -> SIMPLE;
            case "file" -> FILE;
            case "syn" -> SYN;
            default -> throw new IllegalArgumentException("Unknown message type: " + type);
        };
    }

    MessageType(String type) {
        this.type = type;
    }
}
