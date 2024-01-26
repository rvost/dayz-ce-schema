package io.github.rvost.lemminx.dayz.commands;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.CfgEventSpawnsModel;
import io.github.rvost.lemminx.dayz.participants.ParticipantsUtils;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.services.IXMLDocumentProvider;
import org.eclipse.lemminx.services.extensions.commands.AbstractDOMDocumentCommandHandler;
import org.eclipse.lemminx.services.extensions.commands.ArgumentsUtils;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lemminx.utils.TextEditUtils;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.List;

public class ComputeEventsSpawnsCopyHandler extends AbstractDOMDocumentCommandHandler {
    public static final String COMMAND = "dayz-ce-schema.computeEventsSpawnsCopy";
    private final DayzMissionService missionService;

    public ComputeEventsSpawnsCopyHandler(IXMLDocumentProvider documentProvider, DayzMissionService missionService) {
        super(documentProvider);
        this.missionService = missionService;
    }

    @Override
    protected Object executeCommand(DOMDocument document, ExecuteCommandParams params, SharedSettings sharedSettings, CancelChecker cancelChecker) throws Exception {
        var selectedRange = ArgumentsUtils.getArgAt(params, 1, Range.class);
        var prepend = ArgumentsUtils.getArgAt(params, 2, Boolean.class);

        var targetUri = missionService.missionRoot.resolve(CfgEventSpawnsModel.CFGEVENTSPAWNS_FILE).toUri();
        var targetDocument = org.eclipse.lemminx.utils.DOMUtils.loadDocument(
                targetUri.toString(),
                document.getResolverExtensionManager()
        );

        if (targetDocument == null) {
            return null;
        }

        List<DOMNode> selectedNodes = List.of();
        try {
            var startOffset = document.offsetAt(selectedRange.getStart());
            var endOffset = document.offsetAt(selectedRange.getEnd());

            selectedNodes = document.getDocumentElement().getChildren().stream()
                    .filter(n -> ParticipantsUtils.inRange(n.getStart(), startOffset, endOffset) ||
                            ParticipantsUtils.inRange(n.getEnd(), startOffset, endOffset)
                    )
                    .toList();
        } catch (BadLocationException ignored) {
        }

        if (selectedNodes.isEmpty()) {
            return null;
        }

        var edits = new ArrayList<Either<TextDocumentEdit, ResourceOperation>>();

        var actualStartOffset = selectedNodes.get(0).getStart();
        var actualEndOffset = selectedNodes.get(selectedNodes.size() - 1).getEnd();

        var text = document.getTextDocument().getText().substring(actualStartOffset, actualEndOffset);
        var targetEdit = computeTargetEdit(targetDocument, text, prepend);
        edits.add(Either.forLeft(targetEdit));

        return new WorkspaceEdit(edits);
    }

    private static TextDocumentEdit computeTargetEdit(DOMDocument document, String insertText, boolean prepend) {
        int referenceOffset;
        if (prepend) {
            referenceOffset = document.getDocumentElement().getFirstChild().getStart() - 1;
            insertText = insertText + "\n\t";
        } else {
            referenceOffset = document.getDocumentElement().getLastChild().getEnd() + 1;
            insertText = "\n\t" + insertText;
        }
        var referenceRange = XMLPositionUtility.createRange(referenceOffset, referenceOffset, document);
        var te = new TextEdit(new Range(referenceRange.getEnd(), referenceRange.getEnd()), insertText);
        return TextEditUtils.creatTextDocumentEdit(document, List.of(te));
    }
}
