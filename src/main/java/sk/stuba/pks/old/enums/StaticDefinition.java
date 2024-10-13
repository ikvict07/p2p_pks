package sk.stuba.pks.old.enums;

public enum StaticDefinition {
    WINDOW_SIZE(1000),
    MESSAGE_MAX_SIZE(1024);
    public final int value;

    StaticDefinition(int value) {
        this.value = value;
    }
}
