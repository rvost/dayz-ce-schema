package io.github.rvost.lemminx.dayz.model;

import io.github.rvost.lemminx.dayz.utils.DocumentUtils;
import org.eclipse.lemminx.dom.DOMDocument;

import java.util.Optional;

public class CfgEnvironmentModel {
    public static final String CFGENVIRONMENT_FILE = "cfgenvironment.xml";
    public static final String FILE_TAG = "file";
    public static final String TERRITORY_TAG = "territory";
    public static final String PATH_ATTRIBUTE = "path";
    public static final String USABLE_ATTRIBUTE = "usable";


    public static boolean match(DOMDocument document) {
        return DocumentUtils.filenameMatch(document, CFGENVIRONMENT_FILE);
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
