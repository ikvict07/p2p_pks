package sk.stuba.pks.library.enums;

public enum StaticDefinition {
    MESSAGE_MAX_SIZE(1500);
    public final int value;

    StaticDefinition(int value) {
        this.value = value;
    }
}