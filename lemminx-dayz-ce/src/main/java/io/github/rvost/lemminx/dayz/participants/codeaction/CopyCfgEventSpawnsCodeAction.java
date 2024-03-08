package io.github.rvost.lemminx.dayz.participants.codeaction;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.commands.ClientCommands;
import io.github.rvost.lemminx.dayz.model.CfgEventSpawnsModel;
import io.github.rvost.lemminx.dayz.participants.ParticipantsUtils;
import io.github.rvost.lemminx.dayz.participants.diagnostics.MissionDiagnosticsParticipant;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionParticipant;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionRequest;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;

public class CopyCfgEventSpawnsCodeAction implements ICodeActionParticipant {
    private final DayzMissionService missionService;

    public CopyCfgEventSpawnsCodeAction(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void doCodeActionUnconditional(ICodeActionRequest request, List<CodeAction> codeActions, CancelChecker cancelChecker) throws CancellationException {
        var document = request.getDocument();
        if (!documentMatch(document)) {
            return;
        }

        var range = request.getRange();
        Optional<DOMNode> startNode = ParticipantsUtils.tryGetStartNode(document, range);
        if (startNode.isEmpty()) {
            return;
        }

        var ca = new CodeAction("Copy selected event spawns to the mission");
        ca.setKind(CodeActionKind.Refactor);
        ca.setCommand(new Command("Apply copy", ClientCommands.APPLY_EVENT_SPAWNS_COPY, List.of(range)));
        codeActions.add(ca);
    }

    @Override
    public void doCodeAction(ICodeActionRequest request, List<CodeAction> codeActions, CancelChecker cancelChecker) throws CancellationException {
        var diagnostics = request.getDiagnostic();
        if (ParticipantsUtils.match(diagnostics, MissionDiagnosticsParticipant.EXTERNAL_EVENTSPAWNS_CODE)) {
            var document = request.getDocument();
            var tagOffset = document.getDocumentElement().getOffsetAfterStartTag();
            var range = XMLPositionUtility.selectContent(tagOffset, document);

            var ca = new CodeAction("Copy all event spawns to the mission");
            ca.setKind(CodeActionKind.Refactor);
            ca.setCommand(new Command("Apply copy", ClientCommands.APPLY_EVENT_SPAWNS_COPY, List.of(range)));
            codeActions.add(ca);
        }
    }

    private boolean documentMatch(DOMDocument document) {
        var isEventSpawns = CfgEventSpawnsModel.match(document);
        var isExternal = !missionService.isInMissionFolder(document);
        return isEventSpawns && isExternal;
    }
}
