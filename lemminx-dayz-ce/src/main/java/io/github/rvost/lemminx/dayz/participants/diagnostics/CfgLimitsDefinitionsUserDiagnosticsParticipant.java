package io.github.rvost.lemminx.dayz.participants.diagnostics;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.LimitsDefinitionUserModel;
import io.github.rvost.lemminx.dayz.model.LimitsDefinitionsModel;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lemminx.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.github.rvost.lemminx.dayz.participants.diagnostics.DiagnosticsUtils.ERROR_SOURCE;

public class CfgLimitsDefinitionsUserDiagnosticsParticipant implements IDiagnosticsParticipant {
    private static final String INVALID_LIMIT_DEFINITION_CODE = "invalid_limit_definition";
    private static final String INVALID_LIMIT_DEFINITION_MESSAGE = "%s \"%s\" does not exist.";
    private static final String DUPLICATE_LIMIT_DEFINITION_CODE = "duplicate_limit_definition";
    private static final String DUPLICATE_LIMIT_DEFINITION_MESSAGE = "%s \"%s\" already used in this definition.";
    private static final String EMPTY_DEFINITION_CODE = "empty_user_limit_definition";
    private static final String EMPTY_DEFINITION_MESSAGE = "User flag \"%s\" is empty.";
    private final DayzMissionService missionService;

    public CfgLimitsDefinitionsUserDiagnosticsParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void doDiagnostics(DOMDocument domDocument, List<Diagnostic> list, XMLValidationSettings xmlValidationSettings, CancelChecker cancelChecker) {
        if (LimitsDefinitionUserModel.match(domDocument) && missionService.isInMissionFolder(domDocument)) {
            validateUserLimitsDefinitions(domDocument, list);
        }
    }

    private void validateUserLimitsDefinitions(DOMDocument document, List<Diagnostic> diagnostics) {
        var limitsDefinitions = missionService.getLimitsDefinitions();
        for (var node : document.getDocumentElement().getChildren()) {
            var nodeName = node.getNodeName();
            if (node.hasChildNodes()) {
                switch (nodeName) {
                    case LimitsDefinitionUserModel.USAGEFLAGS_TAG -> {
                        validateUserNodes(node.getChildren(), limitsDefinitions.get(LimitsDefinitionsModel.USAGE_TAG), diagnostics);
                    }
                    case LimitsDefinitionUserModel.VALUEFLAGS_TAG -> {
                        validateUserNodes(node.getChildren(), limitsDefinitions.get(LimitsDefinitionsModel.VALUE_TAG), diagnostics);
                    }
                    default -> {
                    }
                }
            }
        }
    }

    private static void validateUserNodes(List<DOMNode> nodes, Set<String> availableValues, List<Diagnostic> diagnostics) {
        for (var node : nodes) {
            if (LimitsDefinitionUserModel.USER_TAG.equals(node.getNodeName())) {
                if (node.hasChildNodes()) {
                    var visited = new HashSet<String>();
                    for (var limitNode : node.getChildren()) {
                        if (limitNode.hasAttribute(LimitsDefinitionsModel.NAME_ATTRIBUTE)) {
                            var kind = limitNode.getNodeName();
                            var attr = limitNode.getAttributeNode(LimitsDefinitionsModel.NAME_ATTRIBUTE);
                            var value = attr.getValue();
                            if (visited.contains(value)) {
                                var range = XMLPositionUtility.createRange(limitNode.getStart(), limitNode.getEnd(), limitNode.getOwnerDocument());
                                String message = String.format(DUPLICATE_LIMIT_DEFINITION_MESSAGE, kind, attr.getValue());
                                diagnostics.add(new Diagnostic(range, message, DiagnosticSeverity.Error, ERROR_SOURCE, DUPLICATE_LIMIT_DEFINITION_CODE));
                            } else if (!availableValues.contains(value)) {
                                var attrValue = attr.getNodeAttrValue();
                                var range = XMLPositionUtility.createRange(attrValue);
                                String message = String.format(INVALID_LIMIT_DEFINITION_MESSAGE, kind, attr.getValue());
                                diagnostics.add(new Diagnostic(range, message, DiagnosticSeverity.Error, ERROR_SOURCE, INVALID_LIMIT_DEFINITION_CODE));
                            }
                            visited.add(value);
                        }
                    }
                    if (visited.isEmpty()){
                        var name = node.getAttribute(LimitsDefinitionsModel.NAME_ATTRIBUTE);
                        var range = XMLPositionUtility.createRange(node.getStart(), node.getEnd(), node.getOwnerDocument());
                        String message = String.format(EMPTY_DEFINITION_MESSAGE, name);
                        diagnostics.add(new Diagnostic(range, message, DiagnosticSeverity.Warning, ERROR_SOURCE, EMPTY_DEFINITION_CODE));
                    }
                } else {
                    var name = node.getAttribute(LimitsDefinitionsModel.NAME_ATTRIBUTE);
                    var range = XMLPositionUtility.createRange(node.getStart(), node.getEnd(), node.getOwnerDocument());
                    String message = String.format(EMPTY_DEFINITION_MESSAGE, name);
                    diagnostics.add(new Diagnostic(range, message, DiagnosticSeverity.Warning, ERROR_SOURCE, EMPTY_DEFINITION_CODE));
                }
            }
        }
    }
}
