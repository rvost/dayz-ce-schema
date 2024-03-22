package io.github.rvost.lemminx.dayz.model;

import io.github.rvost.lemminx.dayz.utils.DocumentUtils;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lsp4j.Range;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class CfgEventGroupsModel {
    public static final String CFGEVENTGROUPS_FILE = "cfgeventgroups.xml";
    public static final String GROUP_TAG = "group";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String TYPE_ATTRIBUTE = "type";
    public static final String SPAWNSECONDARY_ATTRIBUTE = "spawnsecondary";

    public static boolean match(DOMDocument document) {
        return DocumentUtils.filenameMatch(document, CFGEVENTGROUPS_FILE);
    }

    public static Map<String, Range> getCfgEventGroups(Path missionPath) {
        var filePath = missionPath.resolve(CFGEVENTGROUPS_FILE);
        return DocumentUtils.tryParseDocument(filePath)
                .map(CfgEventGroupsModel::getCfgEventGroups)
                .orElse(Map.of());
    }

    public static Map<String, Range> getCfgEventGroups(DOMDocument doc) {
        return DocumentUtils.indexByAttribute(doc, NAME_ATTRIBUTE);
    }

    public static Map<String, List<Range>> getChildTypesIndex(Path missionPath) {
        var filePath = missionPath.resolve(CFGEVENTGROUPS_FILE);
        return DocumentUtils.tryParseDocument(filePath)
                .map(CfgEventGroupsModel::getChildTypesIndex)
                .orElse(Map.of());
    }

    public static Map<String, List<Range>> getChildTypesIndex(DOMDocument doc) {
        return DocumentUtils.indexChildrenByAttribute(doc, TYPE_ATTRIBUTE);
    }
}
