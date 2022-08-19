package com.projectkorra.projectkorra.util;

public enum OptionalBoolean {

    TRUE(true), FALSE(false), DEFAULT(null);

    private final Boolean value;

    OptionalBoolean(final Boolean value) {
        this.value = value;
    }

    public boolean isPresent() {
        return this.value != null;
    }

    public boolean getValue() {
        return value;
    }

    public static OptionalBoolean of(final Boolean value) {
        return value == null ? DEFAULT : value ? TRUE : FALSE;
    }
}
