package io.github.rvost.lemminx.dayz.participants.codeaction;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.commands.ClientCommands;
import io.github.rvost.lemminx.dayz.model.RandomPresetsModel;
import io.github.rvost.lemminx.dayz.model.SpawnableTypesModel;
import io.github.rvost.lemminx.dayz.participants.ParticipantsUtils;
import org.eclipse.lemminx.commons.CodeActionFactory;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionParticipant;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionRequest;
import org.eclipse.lemminx.utils.DOMUtils;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
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

        if (!SpawnableTypesModel.match(document) || !missionService.isInMissionFolder(document)) {
            return;
        }

        var selectedRange = request.getRange();
        var startNode = ParticipantsUtils.tryGetNodeAtSelection(document, selectedRange);
        startNode.ifPresent(node -> computeCodeActions(node, document, codeActions, cancelChecker));
    }

    private void computeCodeActions(DOMNode node, DOMDocument document, List<CodeAction> codeActions, CancelChecker cancelChecker) {
        if (!nodeMatch(node)) {
            return;
        }
        if (!node.hasAttribute(SpawnableTypesModel.PRESET_ATTRIBUTE)) {
            var range = XMLPositionUtility.createRange(node.getStart(), node.getEnd(), document);
            var ca = new CodeAction("Extract random preset");
            ca.setKind(CodeActionKind.RefactorExtract);
            ca.setCommand(new Command("Extract random preset", ClientCommands.APPLY_EXTRACT_RANDOM_PRESET, List.of(range)));
            codeActions.add(ca);
        } else {
            var inlineCa = computeInlineRandomPresetAction(document, node);
            inlineCa.ifPresent(codeActions::add);
        }
    }

    private static boolean nodeMatch(DOMNode node) {
        var name = node.getLocalName();
        return SpawnableTypesModel.ATTACHMENTS_TAG.equals(name) || SpawnableTypesModel.CARGO_TAG.equals(name);
    }

    private Optional<CodeAction> computeInlineRandomPresetAction(DOMDocument document, DOMNode targetNode) {
        var presetsUri = missionService.missionRoot.resolve(RandomPresetsModel.CFGRANDOMPRESETS_FILE).toUri();
        var presetsDocument = DOMUtils.loadDocument(presetsUri.toString(), document.getResolverExtensionManager());

        var presetName = targetNode.getAttribute(SpawnableTypesModel.PRESET_ATTRIBUTE);
        var presetNode = presetsDocument.getDocumentElement().getChildren().stream()
                .filter(n -> presetName.equals(n.getAttribute(RandomPresetsModel.NAME_ATTRIBUTE)))
                .findFirst();

        if (presetNode.isEmpty()) {
            return Optional.empty();
        }

        var node = presetNode.get();
        var insertText = presetsDocument.getTextDocument().getText().substring(node.getStart(), node.getEnd());
        insertText = insertText.replaceFirst("name=\"\\w*\"", "");
        var insertRange = XMLPositionUtility.createRange(targetNode.getStart(), targetNode.getEnd(), document);

        var we = CodeActionFactory.getReplaceWorkspaceEdit(insertText, insertRange, document.getTextDocument());
        var ca = new CodeAction("Inline random preset");
        ca.setKind(CodeActionKind.RefactorInline);
        ca.setEdit(we);

        return Optional.of(ca);
    }
}
