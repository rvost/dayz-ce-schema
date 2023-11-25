package io.github.rvost.lemminx.dayz.model;

import java.util.Optional;

public enum DayzFileType {
    TYPES,
    SPAWNABLETYPES,
    GLOBALS,
    ECONOMY,
    EVENTS,
    MESSAGES;

    public static Optional<DayzFileType> optionalOf(String value) {
        try {
            return Optional.of(DayzFileType.valueOf(value.toUpperCase()));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
}
