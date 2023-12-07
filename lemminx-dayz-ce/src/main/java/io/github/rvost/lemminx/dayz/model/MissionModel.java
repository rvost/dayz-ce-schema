package io.github.rvost.lemminx.dayz.model;

import org.eclipse.lemminx.dom.DOMDocument;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

public class MissionModel {
    public static final String DB_FOLDER = "db";

    public static final Set<String> DEFAULT_FILENAMES = Set.of(
            TypesModel.TYPES_FILE,
            SpawnableTypesModel.SPAWNABLETYPES_FILE,
            GlobalsModel.GLOBALS_FILE,
            EconomyModel.ECONOMY_FILE,
            EventsModel.EVENTS_FILE,
            MessagesModel.MESSAGES_FILE
    );

    public static boolean IsCustomFile(DOMDocument document) {
        var rootTag = document.getDocumentElement();
        return Arrays.stream(DayzFileType.values()).anyMatch(v -> v.RootTag.equals(rootTag.getNodeName()));
    }

    public static Optional<DayzFileType> TryGetFileType(DOMDocument document){
        var rootTag = document.getDocumentElement();
        return Arrays.stream(DayzFileType.values())
                .filter(v -> v.RootTag.equals(rootTag.getNodeName()))
                .findFirst();
    }
}
