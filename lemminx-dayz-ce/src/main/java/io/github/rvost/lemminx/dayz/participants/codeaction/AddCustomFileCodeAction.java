package io.github.rvost.lemminx.dayz.participants.codeaction;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.CfgEconomyCoreModel;
import io.github.rvost.lemminx.dayz.model.DayzFileType;
import io.github.rvost.lemminx.dayz.participants.IndentUtils;
import io.github.rvost.lemminx.dayz.participants.diagnostics.MissionDiagnosticsParticipant;
import org.eclipse.lemminx.commons.CodeActionFactory;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionParticipant;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionRequest;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * This code action adresses 3 main situations:
 * 1) Add a file to an existing empty ce section
 * 2) Add a file to an existing ce section with children
 * 3) Add a file and new ce section
 */
public class AddCustomFileCodeAction implements ICodeActionParticipant {
    private static final String FILE_TAG_FORMAT = "<file name=\"%s\" type=\"%s\"/>\n";
    private static final String CE_TAG_FORMAT =
            "\n<ce folder=\"%s\">" +
                    "\n\t<file name=\"%s\" type=\"%s\"/>" +
                    "\n</ce>";
    private final DayzMissionService missionService;

    public AddCustomFileCodeAction(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void doCodeAction(ICodeActionRequest request, List<CodeAction> codeActions, CancelChecker cancelChecker) {
        var diagnostic = request.getDiagnostic();
        if (!match(diagnostic, MissionDiagnosticsParticipant.FILE_NOT_REGISTERED_CODE)) {
            return;
        }

        var document = request.getDocument();
        var rootTag = document.getDocumentElement();
        var docType = Arrays.stream(DayzFileType.values())
                .filter(v -> v.RootTag.equals(rootTag.getNodeName()))
                .findFirst();
        try {
            var docPath = Path.of(new URI(document.getDocumentURI()));
            var isRegistered = missionService.isRegistered(docPath);
            if (docType.isPresent() && !isRegistered) {
                computeCodeAction(document, docType.get(), docPath, codeActions, diagnostic);
            }
        } catch (URISyntaxException ignored) {
        }
    }

    private void computeCodeAction(DOMDocument document, DayzFileType docType, Path docPath, List<CodeAction> codeActions,
                                   Diagnostic diagnostic) {
        var cfgEconomyDocument = org.eclipse.lemminx.utils.DOMUtils.loadDocument(
                getCfgEconomyCoreURI(),
                document.getResolverExtensionManager()
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

        var addFileAction = CodeActionFactory.insert(
                "Register file in the cfgeconomycore.xml",
                referenceRange.getEnd(),
                insertText,
                cfgEconomyDocument.getTextDocument(),
                diagnostic
        );
        codeActions.add(addFileAction);
    }

    private String getCfgEconomyCoreURI() {
        return missionService.missionRoot.resolve(CfgEconomyCoreModel.CFGECONOMYCORE_XML).toUri().toString();
    }

    public static boolean match(Diagnostic diagnostic, String code) {
        if (diagnostic == null || diagnostic.getCode() == null || !diagnostic.getCode().isLeft()) {
            return false;
        }

        return code == null ? diagnostic.getCode().getLeft() == null :
                code.equals(diagnostic.getCode().getLeft());
    }
}
