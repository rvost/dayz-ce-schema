package io.github.rvost.lemminx.dayz.model;

import org.eclipse.lemminx.dom.DOMDocument;

import java.util.Optional;

public class CfgEnvironmentModel {
    public static final String CFGENVIRONMENT_FILE = "cfgenvironment.xml";
    public static final String FILE_TAG = "file";
    public static final String TERRITORY_TAG = "territory";
    public static final String PATH_ATTRIBUTE = "path";
    public static final String USABLE_ATTRIBUTE = "usable";


    public static boolean isCfgEnvironment(DOMDocument document) {
        if (document == null) {
            return false;
        }
        var uri = document.getDocumentURI();
        return uri != null && uri.toLowerCase().endsWith(CFGENVIRONMENT_FILE);
    }

    public static Optional<String> getUsableKeyFromPath(String path) {
        int lastSlashIndex = path.lastIndexOf('/');
        int firstDotIndex = path.indexOf('.', lastSlashIndex);
        if (firstDotIndex > 0 && lastSlashIndex < firstDotIndex) {
            String k = path.substring(lastSlashIndex + 1, firstDotIndex);
            return Optional.of(k);
        } else {
            return Optional.<String>empty();
        }
    }
}
