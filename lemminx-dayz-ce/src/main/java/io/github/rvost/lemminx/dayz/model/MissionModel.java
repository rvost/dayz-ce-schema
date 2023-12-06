package io.github.rvost.lemminx.dayz.model;

import org.eclipse.lemminx.dom.DOMDocument;

import java.util.Arrays;
import java.util.Optional;

public class MissionModel {
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
