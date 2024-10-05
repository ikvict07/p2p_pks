package sk.stuba.pks.enums;

public enum StaticDefinition {
    WINDOW_SIZE(500),
    MESSAGE_MAX_SIZE(1024);
    public final int value;

    StaticDefinition(int value) {
        this.value = value;
    }
}
