package io.github.rvost.lemminx.dayz.participants.diagnostics;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.MapGroupPosModel;
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

public class MapGroupPosDiagnosticsParticipant implements IDiagnosticsParticipant {
    private static final String INVALID_GROUP_REFERENCE_CODE = "invalid_map_group_reference";
    private static final String INVALID_GROUP_REFERENCE_MESSAGE = "Group \"%s\" does not exist.";
    private final DayzMissionService missionService;

    public MapGroupPosDiagnosticsParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void doDiagnostics(DOMDocument domDocument, List<Diagnostic> list, XMLValidationSettings xmlValidationSettings, CancelChecker cancelChecker) {
        if (MapGroupPosModel.match(domDocument) && missionService.isInMissionFolder(domDocument)) {
            validate(domDocument, list, cancelChecker);
        }
    }

    private void validate(DOMDocument document, List<Diagnostic> diagnostics, CancelChecker cancelChecker) {
        var groups = missionService.getMapGroups();

        document.getDocumentElement().getChildren().stream()
                .map(n -> n.getAttributeNode(MapGroupPosModel.NAME_ATTRIBUTE))
                .filter(Objects::nonNull)
                .filter(attr -> !groups.contains(attr.getValue()))
                .map(attr -> {
                    var attrValueRange = attr.getNodeAttrValue();
                    var range = XMLPositionUtility.createRange(attrValueRange);
                    var message = String.format(INVALID_GROUP_REFERENCE_MESSAGE, attr.getValue());
                    return new Diagnostic(range, message, DiagnosticSeverity.Warning, ERROR_SOURCE, INVALID_GROUP_REFERENCE_CODE);
                })
                .forEach(diagnostics::add);
    }
}
