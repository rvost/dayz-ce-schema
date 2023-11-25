package io.github.rvost.lemminx.dayz.participants.diagnostics;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.RandomPresetsModel;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lemminx.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.List;

import static io.github.rvost.lemminx.dayz.participants.diagnostics.DiagnosticsUtils.ERROR_SOURCE;

public class CfgRandomPresetsDiagnosticsParticipant implements IDiagnosticsParticipant {
    private final DayzMissionService missionService;

    public CfgRandomPresetsDiagnosticsParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void doDiagnostics(DOMDocument domDocument, List<Diagnostic> list, XMLValidationSettings xmlValidationSettings, CancelChecker cancelChecker) {
        if (RandomPresetsModel.isRandomPresets(domDocument)) {
            validateRandomPresets(domDocument, list);
        }
    }

    private void validateRandomPresets(DOMDocument document, List<Diagnostic> diagnostics) {
        for (var presetNode : document.getDocumentElement().getChildren()) {
            for (var itemNode : presetNode.getChildren()) {
                if (itemNode.hasAttribute(RandomPresetsModel.NAME_ATTRIBUTE)) {
                    var attr = itemNode.getAttributeNode(RandomPresetsModel.NAME_ATTRIBUTE);
                    if (!missionService.hasType(attr.getValue())) {
                        var attrValue = attr.getNodeAttrValue();
                        var range = XMLPositionUtility.createRange(attrValue);
                        String message = String.format(DiagnosticsUtils.UNRECOGNISED_TYPE_MESSAGE, attr.getValue());
                        diagnostics.add(new Diagnostic(range, message, DiagnosticSeverity.Warning, ERROR_SOURCE, DiagnosticsUtils.UNRECOGNISED_TYPE_CODE));
                    }
                }
            }
        }

    }
}
