package io.github.rvost.lemminx.dayz.participants.codeaction;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.commands.ClientCommands;
import io.github.rvost.lemminx.dayz.model.DayzFileType;
import io.github.rvost.lemminx.dayz.model.MissionModel;
import io.github.rvost.lemminx.dayz.participants.ParticipantsUtils;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionParticipant;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionRequest;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;

public class RefactorCustomFilesCodeAction implements ICodeActionParticipant {

    private final DayzMissionService missionService;

    public RefactorCustomFilesCodeAction(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void doCodeActionUnconditional(ICodeActionRequest request, List<CodeAction> codeActions, CancelChecker cancelChecker) throws CancellationException {
        var document = request.getDocument();
        var docType = MissionModel.TryGetFileType(document);
        if (docType.isEmpty()) {
            return;
        }
        var docUri = ParticipantsUtils.toURI(document.getDocumentURI());
        if (docUri == null) {
            return;
        }

        var range = request.getRange();
        Optional<DOMNode> startNode = ParticipantsUtils.tryGetStartNode(document, range);
        if (startNode.isEmpty()) {
            return;
        }

        var options = getOptions(docType.get(), docUri);

        var isRegistered = false;
        try {
            var docPath = Path.of(new URI(document.getDocumentURI()));
            isRegistered = missionService.isRegistered(docPath);
        } catch (URISyntaxException ignored) {

        }

        if (isRegistered) {
            generateCodeActionsForMissionFile(codeActions, range, options, docType.get());
        } else {
            generateActionsForExternalFile(codeActions, range, options, docType.get());
        }

    }

    private List<String> getOptions(DayzFileType docType, URI docUri) {
        return missionService.getRegisteredFiles(docType)
                .map(Path::toUri)
                .map(URI::normalize)
                .filter(s -> !s.equals(docUri.normalize()))
                .map(URI::toString)
                .toList();
    }

    private static void generateCodeActionsForMissionFile(List<CodeAction> codeActions,
                                                          Range range,
                                                          List<String> options,
                                                          DayzFileType docType) {
        var move = new CodeAction(String.format("Move %s to...", docType.RootTag));
        move.setKind(CodeActionKind.Refactor + ".move");
        move.setCommand(
                new Command("Apply refactor",
                        ClientCommands.APPLY_CUSTOM_FILES_REFACTOR,
                        List.of("move", range, options, docType))
        );
        move.setDiagnostics(List.of());
        codeActions.add(move);

        var override = new CodeAction(String.format("Override %s in...", docType.RootTag));
        override.setKind(CodeActionKind.RefactorRewrite);
        override.setCommand(
                new Command("Apply refactor",
                        ClientCommands.APPLY_CUSTOM_FILES_REFACTOR,
                        List.of("override", range, options, docType))
        );
        override.setDiagnostics(List.of());
        codeActions.add(override);
    }

    private void generateActionsForExternalFile(List<CodeAction> codeActions,
                                                Range range,
                                                List<String> options,
                                                DayzFileType docType) {
        var copy = new CodeAction(String.format("Copy %s to...", docType.RootTag));
        copy.setKind(CodeActionKind.RefactorExtract);
        copy.setCommand(
                new Command("Apply refactor",
                        ClientCommands.APPLY_CUSTOM_FILES_REFACTOR,
                        List.of("override", range, options, docType))
        );
        copy.setDiagnostics(List.of());
        codeActions.add(copy);
    }
}
