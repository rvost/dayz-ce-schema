package io.github.rvost.lemminx.dayz.participants.codeaction;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.commands.ClientCommands;
import io.github.rvost.lemminx.dayz.model.DayzFileType;
import io.github.rvost.lemminx.dayz.model.MissionModel;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionParticipant;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionRequest;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
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

        var isRegistered = false;
        try {
            var docPath = Path.of(new URI(document.getDocumentURI()));
            isRegistered = missionService.isRegistered(docPath);
        } catch (URISyntaxException ignored) {

        }

        if (docType.isEmpty() || !isRegistered) {
            return;
        }

        Optional<DOMNode> startNode = Optional.empty();
        var range = request.getRange();
        try {

            var startOffset = document.offsetAt(range.getStart());
            var endOffset = document.offsetAt(range.getEnd());

            startNode = document.getDocumentElement().getChildren().stream()
                    .filter(n -> inRange(n.getStart(), startOffset, endOffset) || inRange(n.getEnd(), startOffset, endOffset))
                    .findAny();
        } catch (BadLocationException ignored) {

        }

        if (startNode.isEmpty()) {
            return;
        }

        // TODO: Refactor
        URI docUri = null;
        try {
            docUri = new URI(document.getDocumentURI());
            var p = Path.of(docUri);
            docUri = p.toUri();
        } catch (URISyntaxException e) {
            return;
        }
        var options = getOptions(docType.get(), docUri);

        var move = new CodeAction(String.format("Move %s to...", docType.get().RootTag));
        move.setKind(CodeActionKind.Refactor + ".move");
        move.setCommand(
                new Command("Apply refactor",
                        ClientCommands.APPLY_CUSTOM_FILES_REFACTOR,
                        List.of("move", range, options, docType.get()))
        );
        move.setDiagnostics(List.of());
        codeActions.add(move);

        var override = new CodeAction(String.format("Override %s in...", docType.get().RootTag));
        override.setKind(CodeActionKind.RefactorRewrite);
        override.setCommand(
                new Command("Apply refactor",
                        ClientCommands.APPLY_CUSTOM_FILES_REFACTOR,
                        List.of("override", range, options, docType.get()))
        );
        override.setDiagnostics(List.of());
        codeActions.add(override);

    }

    private List<String> getOptions(DayzFileType docType, URI docUri) {
        return missionService.getRegisteredFiles(docType)
                .map(Path::toUri)
                .map(URI::normalize)
                .filter(s -> !s.equals(docUri.normalize()))
                .map(URI::toString)
                .toList();
    }

    private static boolean inRange(int offset, int startOffset, int endOffset) {
        return offset >= startOffset && offset <= endOffset;
    }
}
