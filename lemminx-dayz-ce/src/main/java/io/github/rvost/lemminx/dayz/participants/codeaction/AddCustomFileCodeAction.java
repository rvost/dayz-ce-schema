package io.github.rvost.lemminx.dayz.participants.codeaction;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.CfgEconomyCoreModel;
import io.github.rvost.lemminx.dayz.model.DayzFileType;
import io.github.rvost.lemminx.dayz.model.MissionModel;
import io.github.rvost.lemminx.dayz.participants.IndentUtils;
import io.github.rvost.lemminx.dayz.participants.ParticipantsUtils;
import io.github.rvost.lemminx.dayz.participants.diagnostics.MissionDiagnosticsParticipant;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionParticipant;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionRequest;
import org.eclipse.lemminx.utils.TextEditUtils;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.eclipse.lemminx.client.ClientCommands.OPEN_URI;

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
        var document = request.getDocument();
        var docType = MissionModel.TryGetFileType(document);

        try {
            var docPath = Path.of(new URI(document.getDocumentURI()));
            var isRegistered = missionService.isRegistered(docPath);

            if (docType.isEmpty() || isRegistered) {
                return;
            }

            var edits = new ArrayList<Either<TextDocumentEdit, ResourceOperation>>();
            Command command = null;
            if (ParticipantsUtils.match(diagnostic, MissionDiagnosticsParticipant.FILE_NOT_REGISTERED_CODE)) {
                var edit = getCfgEconomyEdit(document, docPath, docType.get());
                edits.add(Either.forLeft(edit));
            }
            if (ParticipantsUtils.match(diagnostic, MissionDiagnosticsParticipant.FILE_OUT_OF_FOLDER_CODE)) {
                var newPath = getNewDocumentPath(docPath);

                var createFileEdit = getCreateFileOperation(newPath);
                edits.add(Either.forRight(createFileEdit));

                var copyFileContentEdit = copyFileContentEdit(document, newPath);
                edits.add(Either.forLeft(copyFileContentEdit));

                var cfgEconomyEdit = getCfgEconomyEdit(document, newPath, docType.get());
                edits.add(Either.forLeft(cfgEconomyEdit));

                command = new Command("Open file", OPEN_URI, List.of(newPath.toUri().toString()));
            }

            if (!edits.isEmpty()) {
                var ca = new CodeAction("Add file to the mission");
                ca.setKind(CodeActionKind.QuickFix);
                ca.setDiagnostics(List.of(diagnostic));
                ca.setEdit(new WorkspaceEdit(edits));
                if (command != null) {
                    ca.setCommand(command);
                }
                codeActions.add(ca);
            }
        } catch (URISyntaxException ignored) {
        }
    }

    // TODO: Refactor
    /**
     * This method addresses 3 main situations:
     * 1) Add a file to an existing empty ce section
     * 2) Add a file to an existing ce section with children
     * 3) Add a file and new ce section
     */
    private TextDocumentEdit getCfgEconomyEdit(DOMDocument document, Path docPath, DayzFileType docType) throws URISyntaxException {
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

        var te = new TextEdit(new Range(referenceRange.getEnd(), referenceRange.getEnd()), insertText);
        return TextEditUtils.creatTextDocumentEdit(cfgEconomyDocument, List.of(te));
    }

    private Path getNewDocumentPath(Path originalPath) {
        var filename = originalPath.getFileName().toString();
        var targetFolder = MissionModel.DEFAULT_FILENAMES.contains(filename) ?
                originalPath.getParent().getFileName().toString() :
                MissionModel.DB_FOLDER;

        return missionService.missionRoot.resolve(targetFolder).resolve(filename);
    }

    private ResourceOperation getCreateFileOperation(Path docPath) {
        var opt = new CreateFileOptions(false, true);

        return new CreateFile(docPath.toUri().toString(), opt);
    }

    private TextDocumentEdit copyFileContentEdit(DOMDocument original, Path newDocPath) {
        var identifier = new VersionedTextDocumentIdentifier(newDocPath.toUri().toString(), 0);
        var te = new TextEdit(new Range(new Position(0, 0), new Position(0, 0)), original.getText());
        return new TextDocumentEdit(identifier, Collections.singletonList(te));
    }

    private String getCfgEconomyCoreURI() {
        return missionService.missionRoot.resolve(CfgEconomyCoreModel.CFGECONOMYCORE_XML).toUri().toString();
    }

}
