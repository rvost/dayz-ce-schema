package io.github.rvost.lemminx.dayz.participants.diagnostics;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.CfgEventGroupsModel;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lemminx.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.List;
import java.util.stream.Collectors;

import static io.github.rvost.lemminx.dayz.participants.diagnostics.DiagnosticsUtils.ERROR_SOURCE;

public class CfgEventGroupsDiagnosticsParticipant implements IDiagnosticsParticipant {
    private static final String INVALID_TYPE_REFERENCE_CODE = "invalid_type_reference";
    private static final String INVALID_TYPE_REFERENCE_MESSAGE = "Type \"%s\" does not exist.";
    private static final String INVALID_MAP_GROUP_REFERENCE_CODE = "invalid_map_group_reference";
    private static final String INVALID_MAP_GROUP_REFERENCE_MESSAGE = "May be invalid group reference. Map group prototype \"%s\" not found.";

    private final DayzMissionService missionService;

    public CfgEventGroupsDiagnosticsParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void doDiagnostics(DOMDocument domDocument, List<Diagnostic> list, XMLValidationSettings xmlValidationSettings, CancelChecker cancelChecker) {
        if (CfgEventGroupsModel.match(domDocument)) {
            validateMapGroupReferences(domDocument, list, cancelChecker);
        }
    }

    private void validateMapGroupReferences(DOMDocument document, List<Diagnostic> diagnostics, CancelChecker cancelChecker) {
        var mapGroups = missionService.getMapGroups();
        var types = missionService.getAllTypes().collect(Collectors.toSet());

        var childNodes = document.getDocumentElement().getChildren().stream()
                .flatMap(n -> n.getChildren().stream())
                .filter(n -> n.hasAttribute(CfgEventGroupsModel.TYPE_ATTRIBUTE))
                .toList();

        for (var node : childNodes) {
            var type = node.getAttribute(CfgEventGroupsModel.TYPE_ATTRIBUTE);
            var isInMapGroups = mapGroups.contains(type);

            if (!isInMapGroups) {
                var hasSpawnSecondary = node.hasAttribute(CfgEventGroupsModel.SPAWNSECONDARY_ATTRIBUTE);
                var isInTypes = types.contains(type);
                var attr = node.getAttributeNode(CfgEventGroupsModel.TYPE_ATTRIBUTE);
                if (!isInTypes) {
                    diagnostics.add(toDiagnostic(attr, INVALID_TYPE_REFERENCE_MESSAGE, DiagnosticSeverity.Error, INVALID_TYPE_REFERENCE_CODE));
                }
                if (!hasSpawnSecondary) {
                    diagnostics.add(toDiagnostic(attr, INVALID_MAP_GROUP_REFERENCE_MESSAGE, DiagnosticSeverity.Information, INVALID_MAP_GROUP_REFERENCE_CODE));
                }
            }
            cancelChecker.checkCanceled();
        }
    }

    private static Diagnostic toDiagnostic(DOMAttr attr, String messageFormat, DiagnosticSeverity severity, String code) {
        var attrValueRange = attr.getNodeAttrValue();
        var range = XMLPositionUtility.createRange(attrValueRange);
        var message = String.format(messageFormat, attr.getValue());
        return new Diagnostic(range, message, severity, ERROR_SOURCE, code);

    }
}
