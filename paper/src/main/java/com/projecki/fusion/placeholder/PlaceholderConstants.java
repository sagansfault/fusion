package com.projecki.fusion.placeholder;

/**
 * Constants that are required for all placeholders
 */
public enum PlaceholderConstants {
    VERSION("1.0"),
    AUTHOR("Projecki Dev");

    private final String value;

    PlaceholderConstants(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
