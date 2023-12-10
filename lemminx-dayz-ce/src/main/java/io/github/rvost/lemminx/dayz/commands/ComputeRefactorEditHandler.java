package io.github.rvost.lemminx.dayz.commands;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.participants.IndentUtils;
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

public class ComputeRefactorEditHandler extends AbstractDOMDocumentCommandHandler {
    public static final String COMMAND = "dayz-ce-schema.computeRefactorEdit";

    private final DayzMissionService missionService;

    public ComputeRefactorEditHandler(IXMLDocumentProvider documentProvider, DayzMissionService missionService) {
        super(documentProvider);
        this.missionService = missionService;
    }

    @Override
    protected Object executeCommand(DOMDocument document, ExecuteCommandParams params, SharedSettings sharedSettings, CancelChecker cancelChecker) throws Exception {
        var targetUri = ArgumentsUtils.getArgAt(params, 1, String.class);
        var kind = ArgumentsUtils.getArgAt(params, 2, String.class);
        var selectedRange = ArgumentsUtils.getArgAt(params, 3, Range.class);

        var targetDocument = org.eclipse.lemminx.utils.DOMUtils.loadDocument(
                targetUri,
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
                    .filter(n -> inRange(n.getStart(), startOffset, endOffset) || inRange(n.getEnd(), startOffset, endOffset))
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
        var targetEdit = computeTargetEdit(targetDocument, text);
        edits.add(Either.forLeft(targetEdit));

        if ("move".equals(kind)) {
            var actualRange = XMLPositionUtility.createRange(actualStartOffset, actualEndOffset, document);

            var te = new TextEdit(actualRange, "");
            var removeEdit = TextEditUtils.creatTextDocumentEdit(document, List.of(te));
            edits.add(Either.forLeft(removeEdit));
        }

        return new WorkspaceEdit(edits);
    }

    /**
     * This method addresses 2 situations:
     * 1) Add content to an empty file
     * 2) Append content to non-empty file
     */
    private static TextDocumentEdit computeTargetEdit(DOMDocument document, String text) {
        var insertText = "";
        int referenceRangeStart;
        int referenceRangeEnd;

        var targetNode = document.getDocumentElement();
        var lastChild = targetNode.getLastChild();

        if (targetNode.getChildren().size() > 1) {
            // Situation 2
            insertText = "\n\t" + text;
            referenceRangeStart = lastChild.getStart();
            referenceRangeEnd = lastChild.getEnd();
        } else {
            if (lastChild != null && (lastChild.hasChildNodes() || lastChild.isComment())) {
                // Situation 2
                insertText = "\n\t" + text;
                referenceRangeStart = lastChild.getStart();
                referenceRangeEnd = lastChild.getEnd();
            } else {
                // Situation 1
                insertText = "\n\t" + text;
                referenceRangeStart = targetNode.getStartTagOpenOffset();
                referenceRangeEnd = targetNode.getStartTagCloseOffset() + 1;
            }
        }

        var referenceRange = XMLPositionUtility.createRange(referenceRangeStart, referenceRangeEnd, document);
//        insertText = IndentUtils.formatText(insertText, "\t", referenceRange.getStart().getCharacter());

        var te = new TextEdit(new Range(referenceRange.getEnd(), referenceRange.getEnd()), insertText);
        return TextEditUtils.creatTextDocumentEdit(document, List.of(te));
    }

    private static boolean inRange(int offset, int startOffset, int endOffset) {
        return offset >= startOffset && offset <= endOffset;
    }
}
