package io.github.rvost.lemminx.dayz.participants.diagnostics;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.CfgEventSpawnsModel;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lemminx.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.List;
import java.util.Objects;

import static io.github.rvost.lemminx.dayz.participants.diagnostics.DiagnosticsUtils.ERROR_SOURCE;

public class CfgEventSpawnsDiagnosticsParticipant implements IDiagnosticsParticipant {
    private static final String INVALID_EVENT_REFERENCE_CODE = "invalid_event_reference";
    private static final String INVALID_EVENT_REFERENCE_MESSAGE = "Event \"%s\" does not exist.";
    private final DayzMissionService missionService;

    public CfgEventSpawnsDiagnosticsParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void doDiagnostics(DOMDocument domDocument, List<Diagnostic> list, XMLValidationSettings xmlValidationSettings, CancelChecker cancelChecker) {
        if (CfgEventSpawnsModel.isEventSpawns(domDocument)) {
            validateEventSpawns(domDocument, list);
        }
    }

    private void validateEventSpawns(DOMDocument document, List<Diagnostic> diagnostics) {
        document.getDocumentElement().getChildren().stream()
                .map(n -> n.getAttributeNode(CfgEventSpawnsModel.NAME_ATTRIBUTE))
                .filter(Objects::nonNull)
                .filter(attr -> !missionService.hasEvent(attr.getValue()))
                .map(attr -> {
                    var attrValueRange = attr.getNodeAttrValue();
                    var range = XMLPositionUtility.createRange(attrValueRange);
                    var message = String.format(INVALID_EVENT_REFERENCE_MESSAGE, attr.getValue());
                    return new Diagnostic(range, message, DiagnosticSeverity.Error, ERROR_SOURCE, INVALID_EVENT_REFERENCE_CODE);
                })
                .forEach(diagnostics::add);
    }
}
