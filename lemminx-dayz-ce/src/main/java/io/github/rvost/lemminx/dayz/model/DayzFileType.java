package io.github.rvost.lemminx.dayz.model;

import java.util.Optional;

public enum DayzFileType {
    TYPES("types"),
    SPAWNABLETYPES("spawnabletypes"),
    GLOBALS("variables"),
    ECONOMY("economy"),
    EVENTS("events"),
    MESSAGES("messages");

    public final String RootTag;

    DayzFileType(String rootTag) {
        RootTag = rootTag;
    }

    public static Optional<DayzFileType> optionalOf(String value) {
        try {
            return Optional.of(DayzFileType.valueOf(value.toUpperCase()));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
}
