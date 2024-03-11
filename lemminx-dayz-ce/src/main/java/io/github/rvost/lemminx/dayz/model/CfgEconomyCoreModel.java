package io.github.rvost.lemminx.dayz.model;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.participants.IndentUtils;
import io.github.rvost.lemminx.dayz.utils.DocumentUtils;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.uriresolver.URIResolverExtensionManager;
import org.eclipse.lemminx.utils.TextEditUtils;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CfgEconomyCoreModel {
    public static final String CFGECONOMYCORE_XML = "cfgeconomycore.xml";
    public static final String CE_TAG = "ce";
    public static final String FOLDER_ATTRIBUTE = "folder";
    public static final String FILE_TAG = "file";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String TYPE_ATTRIBUTE = "type";

    private static final String FILE_TAG_FORMAT = "<file name=\"%s\" type=\"%s\"/>\n";
    private static final String CE_TAG_FORMAT =
            "\n<ce folder=\"%s\">" +
                    "\n\t<file name=\"%s\" type=\"%s\"/>" +
                    "\n</ce>";

    public static boolean match(DOMDocument document) {
        return DocumentUtils.filenameMatch(document, CFGECONOMYCORE_XML);
    }

    public static Map<Path, DayzFileType> getCustomFiles(Path missionPath) {
        var path = missionPath.resolve(CFGECONOMYCORE_XML);
        return getCustomFilesFromFile(path, missionPath);
    }

    public static Map<Path, DayzFileType> getCustomFilesFromFile(Path filePath, Path missionPath) {
        return DocumentUtils.tryParseDocument(filePath)
                .map(doc -> {
                    var relativeMap = getCustomFiles(doc);
                    return relativeMap.collect(Collectors.toMap(e -> missionPath.resolve(e.getKey()).toAbsolutePath(),
                            Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue,
                            HashMap::new)
                    );
                })
                .orElse(new HashMap<>());
    }

    private static Stream<SimpleEntry<Path, DayzFileType>> getCustomFiles(DOMDocument document) {
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

    /**
     * This method addresses 3 main situations:
     * 1) Add a file to an existing empty ce section
     * 2) Add a file to an existing ce section with children
     * 3) Add a file and new ce section
     */
    public static TextDocumentEdit getNewFileEdit(Path docPath, DayzFileType docType, DayzMissionService missionService, URIResolverExtensionManager uriResolver) throws URISyntaxException {
        var cfgEconomyDocument = org.eclipse.lemminx.utils.DOMUtils.loadDocument(
                getCfgEconomyCoreURI(missionService),
                uriResolver
        );

        var folder = docPath.getParent().getFileName().toString();

        var insertText = "";
        int referenceRangeStart;
        int referenceRangeEnd;

        var targetCeNode = cfgEconomyDocument.getDocumentElement().getChildren().stream()
                .filter(n -> CfgEconomyCoreModel.CE_TAG.equals(n.getNodeName()))
                .filter(n -> folder.equals(n.getAttribute(CfgEconomyCoreModel.FOLDER_ATTRIBUTE)))
                .findFirst();

        if (targetCeNode.isPresent()) {
            var node = targetCeNode.get();
            var lastChild = node.getLastChild();
            if (node.getChildren().size() > 1) {
                // Situation 2
                insertText = "\n" + FILE_TAG_FORMAT;
                referenceRangeStart = lastChild.getStart();
                referenceRangeEnd = lastChild.getEnd();
            } else {
                if (lastChild != null && (lastChild.hasChildNodes() || lastChild.isComment())) {
                    // Situation 2
                    insertText = "\n" + FILE_TAG_FORMAT;
                    referenceRangeStart = lastChild.getStart();
                    referenceRangeEnd = lastChild.getEnd();
                } else {
                    // Situation 1
                    insertText = "\n" + FILE_TAG_FORMAT;
                    var ceElement = (DOMElement) node;
                    referenceRangeStart = ceElement.getStartTagOpenOffset();
                    referenceRangeEnd = ceElement.getStartTagCloseOffset() + 1;
                }
            }
            insertText = String.format(insertText, docPath.getFileName(), docType.toString().toLowerCase());
        } else {
            // Situation 3
            insertText = String.format(CE_TAG_FORMAT, folder, docPath.getFileName(), docType.toString().toLowerCase());
            var documentElement = cfgEconomyDocument.getDocumentElement();
            referenceRangeStart = documentElement.getLastChild().getStart();
            referenceRangeEnd = documentElement.getLastChild().getEnd();
        }

        var referenceRange = XMLPositionUtility.createRange(referenceRangeStart, referenceRangeEnd, cfgEconomyDocument);
        insertText = IndentUtils.formatText(insertText, "\t", referenceRange.getStart().getCharacter());

        var te = new TextEdit(new Range(referenceRange.getEnd(), referenceRange.getEnd()), insertText);
        return TextEditUtils.creatTextDocumentEdit(cfgEconomyDocument, List.of(te));
    }

    // TODO: Consider removing dependency on DayzMissionService
    public static String getCfgEconomyCoreURI(DayzMissionService missionService) {
        return missionService.missionRoot.resolve(CfgEconomyCoreModel.CFGECONOMYCORE_XML).toUri().toString();
    }
}

