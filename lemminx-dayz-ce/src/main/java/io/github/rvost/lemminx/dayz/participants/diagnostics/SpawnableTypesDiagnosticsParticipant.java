package io.github.rvost.lemminx.dayz.participants.diagnostics;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.SpawnableTypesModel;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lemminx.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.List;

import static io.github.rvost.lemminx.dayz.participants.diagnostics.DiagnosticsUtils.ERROR_SOURCE;

public class SpawnableTypesDiagnosticsParticipant implements IDiagnosticsParticipant {
    private static final String INVALID_RANDOM_PRESET_CODE = "invalid_random_preset";
    private static final String INVALID_RANDOM_PRESET_MESSAGE = "%s preset \"%s\" does not exist.";

    private final DayzMissionService missionService;

    public SpawnableTypesDiagnosticsParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void doDiagnostics(DOMDocument domDocument, List<Diagnostic> list, XMLValidationSettings xmlValidationSettings, CancelChecker cancelChecker) {
        if (SpawnableTypesModel.isSpawnableTypes(domDocument)) {
            validateSpawnableTypes(domDocument, list);
        }
    }

    private void validateSpawnableTypes(DOMDocument document, List<Diagnostic> diagnostics) {
        var randomPresets = missionService.getRandomPresets();
        for (var typeNode : document.getDocumentElement().getChildren()) {
            if (typeNode.hasChildNodes()) {
                for (var node : typeNode.getChildren()) {
                    var kind = node.getNodeName();
                    if (node.hasAttribute(SpawnableTypesModel.PRESET_ATTRIBUTE) && randomPresets.containsKey(kind)) {
                        var attr = node.getAttributeNode(SpawnableTypesModel.PRESET_ATTRIBUTE);
                        if (!randomPresets.get(kind).contains(attr.getValue())) {
                            var attrValue = attr.getNodeAttrValue();
                            var range = XMLPositionUtility.createRange(attrValue);
                            String message = String.format(INVALID_RANDOM_PRESET_MESSAGE, kind, attr.getValue());
                            diagnostics.add(new Diagnostic(range, message, DiagnosticSeverity.Error, ERROR_SOURCE, INVALID_RANDOM_PRESET_CODE));
                        }
                    }
                }
            }
        }
    }
}
