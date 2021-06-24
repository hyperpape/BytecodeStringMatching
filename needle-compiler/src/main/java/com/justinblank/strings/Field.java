package com.justinblank.strings;

import java.util.Objects;

public class Field {

    final int modifier;
    final String name;
    final String descriptor;
    final String signature;
    final Object value;

    public Field(int modifier, String name, String descriptor, String signature, Object value) {
        Objects.requireNonNull(name, descriptor);
        this.modifier = modifier;
        this.name = name;
        this.descriptor = descriptor;
        this.signature = signature;
        this.value = value;
    }
}
