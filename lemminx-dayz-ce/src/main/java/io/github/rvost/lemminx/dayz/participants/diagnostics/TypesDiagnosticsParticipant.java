package io.github.rvost.lemminx.dayz.participants.diagnostics;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.TypesModel;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lemminx.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.github.rvost.lemminx.dayz.participants.diagnostics.DiagnosticsUtils.ERROR_SOURCE;

public class TypesDiagnosticsParticipant implements IDiagnosticsParticipant {
    private static final String INVALID_LIMIT_DEFINITION_CODE = "invalid_limit_definition";
    private static final String INVALID_LIMIT_DEFINITION_MESSAGE = "%s \"%s\" does not exist.";
    private static final String INVALID_USER_LIMIT_DEFINITION_CODE = "invalid_limit_definition";
    private static final String INVALID_USER_LIMIT_DEFINITION_MESSAGE = "User %s flag \"%s\" does not exist.";
    private static final String NAME_ATTRIBUTE_NOT_ALLOWED_CODE = "name_not_allowed";
    private static final String NAME_ATTRIBUTE_NOT_ALLOWED_MESSAGE = "\"name\" attribute is not allowed when \"user\" attribute is specified";
    private static final String MULTIPLE_CATEGORY_NOT_ALLOWED_CODE = "multiple_category_not_allowed";
    private static final String MULTIPLE_CATEGORY_NOT_ALLOWED_MESSAGE = "Multiple categories is not allowed.";

    private final DayzMissionService missionService;

    public TypesDiagnosticsParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void doDiagnostics(DOMDocument domDocument, List<Diagnostic> list, XMLValidationSettings xmlValidationSettings, CancelChecker cancelChecker) {
        if (TypesModel.isTypes(domDocument)) {
            validateTypes(domDocument, list, cancelChecker);
        }
    }

    private void validateTypes(DOMDocument document, List<Diagnostic> diagnostics, CancelChecker cancelChecker) {
        var limitsDefinitions = missionService.getLimitsDefinitions();
        var userLimitsDefinitions = missionService.getUserLimitsDefinitions();

        for (var typeNode : document.getDocumentElement().getChildren()) {
            for (var node : typeNode.getChildren()) {
                var nodeName = node.getNodeName();
                if (limitsDefinitions.containsKey(nodeName) && node.hasAttribute(TypesModel.NAME_ATTRIBUTE)) {
                    validateLimitName(diagnostics, node, limitsDefinitions);
                }
                if (userLimitsDefinitions.containsKey(nodeName) && node.hasAttribute(TypesModel.USER_ATTRIBUTE)) {
                    validateLimitUserName(diagnostics, node, userLimitsDefinitions);
                }
                cancelChecker.checkCanceled();
            }
            cancelChecker.checkCanceled();
            validateCategory(diagnostics, typeNode);
            cancelChecker.checkCanceled();
        }
    }

    private static void validateCategory(List<Diagnostic> diagnostics, DOMNode typeNode) {
        typeNode.getChildren().stream()
                .filter(n -> TypesModel.CATEGORY_TAG.equals(n.getNodeName()))
                .skip(1)
                .map(n -> {
                    var range = XMLPositionUtility.createRange(n.getStart(), n.getEnd(), n.getOwnerDocument());
                    return new Diagnostic(range, MULTIPLE_CATEGORY_NOT_ALLOWED_MESSAGE, DiagnosticSeverity.Error, ERROR_SOURCE, MULTIPLE_CATEGORY_NOT_ALLOWED_CODE);
                })
                .forEach(diagnostics::add);
    }

    private static void validateLimitName(List<Diagnostic> diagnostics, DOMNode node, Map<String, Set<String>> availableDefinitions) {
        var nodeName = node.getNodeName();
        var nameAttr = node.getAttributeNode(TypesModel.NAME_ATTRIBUTE);
        var validValues = availableDefinitions.get(nodeName);
        if (!validValues.contains(nameAttr.getValue())) {
            var attrValue = nameAttr.getNodeAttrValue();
            var range = XMLPositionUtility.createRange(attrValue);
            String message = String.format(INVALID_LIMIT_DEFINITION_MESSAGE, nodeName, nameAttr.getValue());
            diagnostics.add(new Diagnostic(range, message, DiagnosticSeverity.Error, ERROR_SOURCE, INVALID_LIMIT_DEFINITION_CODE));
        }
    }

    private static void validateLimitUserName(List<Diagnostic> diagnostics, DOMNode node, Map<String, Set<String>> availableDefinitions) {
        var nodeName = node.getNodeName();
        var userAttr = node.getAttributeNode(TypesModel.USER_ATTRIBUTE);
        var validValues = availableDefinitions.get(nodeName);
        if (!validValues.contains(userAttr.getValue())) {
            var attrValue = userAttr.getNodeAttrValue();
            var range = XMLPositionUtility.createRange(attrValue);
            String message = String.format(INVALID_USER_LIMIT_DEFINITION_MESSAGE, nodeName, userAttr.getValue());
            diagnostics.add(new Diagnostic(range, message, DiagnosticSeverity.Error, ERROR_SOURCE, INVALID_USER_LIMIT_DEFINITION_CODE));
        }
        if (node.hasAttribute(TypesModel.NAME_ATTRIBUTE)) {
            var nameAttr = node.getAttributeNode(TypesModel.NAME_ATTRIBUTE);
            var range = XMLPositionUtility.createRange(nameAttr);
            diagnostics.add(new Diagnostic(range, NAME_ATTRIBUTE_NOT_ALLOWED_MESSAGE, DiagnosticSeverity.Error, ERROR_SOURCE, NAME_ATTRIBUTE_NOT_ALLOWED_CODE));
        }
    }
}
