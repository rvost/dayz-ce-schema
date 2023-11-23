package io.github.rvost.lemminx.dayz.utils;

import java.nio.file.Path;

public record MissionFolderEvent(EventType type, Path path) {
    public enum EventType {
        UNKNOWN,
        FOLDER_CREATED,
        FOLDER_MODIFIED,
        FOLDER_DELETED,
        FILE_CREATED,
        FILE_DELETED,
        FILE_MODIFIED
    }
}
