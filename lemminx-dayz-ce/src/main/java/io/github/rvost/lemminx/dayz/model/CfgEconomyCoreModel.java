package io.github.rvost.lemminx.dayz.model;

import org.eclipse.lemminx.commons.TextDocument;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.dom.DOMParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CfgEconomyCoreModel {
    public static final String CFGECONOMYCORE_XML = "cfgeconomycore.xml";
    public static final String CE_TAG = "ce";
    public static final String FOLDER_ATTRIBUTE = "folder";
    public static final String FILE_TAG = "file";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String TYPE_ATTRIBUTE = "type";

    public static boolean isCfgEconomyCore(DOMDocument document) {
        if (document == null) {
            return false;
        }
        var uri = document.getDocumentURI();
        return uri != null && uri.toLowerCase().endsWith(CFGECONOMYCORE_XML);
    }

    public static Map<Path, DayzFileType> getCustomFiles(Path missionPath) {
        var path = missionPath.resolve(CFGECONOMYCORE_XML);
        return getCustomFilesFromFile(path, missionPath);
    }

    public static Map<Path, DayzFileType> getCustomFilesFromFile(Path filePath, Path missionPath) {
        try {
            String fileContent = String.join(System.lineSeparator(), Files.readAllLines(filePath));
            DOMDocument doc = DOMParser.getInstance().parse(new TextDocument(fileContent, filePath.toString()), null);
            var relativeMap = getCustomFiles(doc);
            return relativeMap.collect(Collectors.toMap(e -> missionPath.resolve(e.getKey()).toAbsolutePath(),
                    Map.Entry::getValue,
                    (oldValue, newValue) -> oldValue,
                    HashMap::new)
            );
        } catch (IOException e) {
            return Map.of();
        }
    }

    private static Stream<SimpleEntry<Path, DayzFileType>> getCustomFiles(DOMDocument document) throws IOException {
        return document.getDocumentElement().getChildren().stream()
                .filter(e -> CE_TAG.equals(e.getNodeName()))
                .filter(e -> e.hasAttribute(FOLDER_ATTRIBUTE))
                .flatMap(CfgEconomyCoreModel::getFilesForFolder);
    }

    private static Stream<SimpleEntry<Path, DayzFileType>> getFilesForFolder(DOMNode ceNode) {
        var folder = ceNode.getAttribute(FOLDER_ATTRIBUTE);
        return ceNode.getChildren().stream()
                .filter(n -> n.hasAttribute(NAME_ATTRIBUTE) && n.hasAttribute(TYPE_ATTRIBUTE))
                .map(n -> new SimpleEntry<>(n.getAttribute(NAME_ATTRIBUTE),
                        DayzFileType.optionalOf(n.getAttribute(TYPE_ATTRIBUTE))))
                .filter(e -> e.getValue().isPresent())
                .map(e -> new SimpleEntry<>(Path.of(folder, e.getKey()), e.getValue().get()));
    }
}

