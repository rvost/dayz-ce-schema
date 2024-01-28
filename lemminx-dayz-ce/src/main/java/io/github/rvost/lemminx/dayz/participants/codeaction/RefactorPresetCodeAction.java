package io.github.rvost.lemminx.dayz.participants.codeaction;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.commands.ClientCommands;
import io.github.rvost.lemminx.dayz.model.SpawnableTypesModel;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionParticipant;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionRequest;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;

public class RefactorPresetCodeAction implements ICodeActionParticipant {
    private final DayzMissionService missionService;

    public RefactorPresetCodeAction(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void doCodeActionUnconditional(ICodeActionRequest request, List<CodeAction> codeActions, CancelChecker cancelChecker) throws CancellationException {
        var document = request.getDocument();

        if (!SpawnableTypesModel.isSpawnableTypes(document) || !missionService.isInMissionFolder(document)) {
            return;
        }

        var selectedRange = request.getRange();
        var startNode = tryGetNodeAtSelection(document, selectedRange);
        startNode.ifPresent(node -> computeCodeActions(node, document, codeActions, cancelChecker));
    }

    private void computeCodeActions(DOMNode node, DOMDocument document, List<CodeAction> codeActions, CancelChecker cancelChecker) {
        if (nodeMatch(node) && !node.hasAttribute(SpawnableTypesModel.PRESET_ATTRIBUTE)) {
            var range = XMLPositionUtility.createRange(node.getStart(), node.getEnd(), document);
            var ca = new CodeAction("Extract random preset");
            ca.setKind(CodeActionKind.RefactorExtract);
            ca.setCommand(new Command("Extract random preset", ClientCommands.APPLY_EXTRACT_RANDOM_PRESET, List.of(range)));
            codeActions.add(ca);
        }
    }

    private static boolean nodeMatch(DOMNode node) {
        var name = node.getLocalName();
        return SpawnableTypesModel.ATTACHMENTS_TAG.equals(name) || SpawnableTypesModel.CARGO_TAG.equals(name);
    }

    private static Optional<DOMNode> tryGetNodeAtSelection(DOMDocument document, Range range){
        try {
            return  Optional.ofNullable(document.findNodeAt(document.offsetAt(range.getStart())));
        } catch (BadLocationException e) {
            return Optional.empty();
        }
    }
}
