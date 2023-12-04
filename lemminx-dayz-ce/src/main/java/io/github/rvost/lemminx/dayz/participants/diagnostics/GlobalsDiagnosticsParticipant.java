package io.github.rvost.lemminx.dayz.participants.diagnostics;

import io.github.rvost.lemminx.dayz.model.GlobalsModel;
import io.github.rvost.lemminx.dayz.model.GlobalsValidationError;
import io.github.rvost.lemminx.dayz.model.GlobalsVariable;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lemminx.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.List;

import static io.github.rvost.lemminx.dayz.participants.diagnostics.DiagnosticsUtils.ERROR_SOURCE;

public class GlobalsDiagnosticsParticipant implements IDiagnosticsParticipant {
    private static final String INVALID_GLOBALS_VAR_TYPE_CODE = "invalid_globals_var_type";
    private static final String INVALID_GLOBALS_VAR_VALUE_CODE = "invalid_globals_var_value";
    private static final String INVALID_GLOBALS_TYPE_CODE = "invalid_globals_type";
    private static final String INVALID_GLOBALS_TYPE_MESSAGE = "Invalid type for variable. Expected %s.";

    @Override
    public void doDiagnostics(DOMDocument domDocument, List<Diagnostic> list, XMLValidationSettings xmlValidationSettings, CancelChecker cancelChecker) {
        if (GlobalsModel.isGlobals(domDocument)) {
            validateGlobals(domDocument, list);
        }
    }

    private void validateGlobals(DOMDocument document, List<Diagnostic> diagnostics) {
        for (var node : document.getDocumentElement().getChildren()) {
            if (node.hasAttribute(GlobalsModel.NAME_ATTRIBUTE)
                    && node.hasAttribute(GlobalsModel.TYPE_ATTRIBUTE)
                    && node.hasAttribute(GlobalsModel.VALUE_ATTRIBUTE)) {
                var name = node.getAttribute(GlobalsModel.NAME_ATTRIBUTE);
                try {
                    var varibale = GlobalsVariable.valueOf(name);
                    var valueAttr = node.getAttributeNode(GlobalsModel.VALUE_ATTRIBUTE);
                    var value = valueAttr.getValue();
                    var range = XMLPositionUtility.createRange(valueAttr.getNodeAttrValue());

                    varibale.validate(value)
                            .map(err -> toDiagnostic(range, err))
                            .forEach(diagnostics::add);
                    var typeAttr = node.getAttributeNode(GlobalsModel.TYPE_ATTRIBUTE);
                    if (!varibale.typeCode.equals(typeAttr.getValue())) {
                        var typeRange = XMLPositionUtility.createRange(typeAttr.getNodeAttrValue());
                        var message = String.format(INVALID_GLOBALS_TYPE_MESSAGE, varibale.typeCode);
                        diagnostics.add(new Diagnostic(typeRange, message, DiagnosticSeverity.Error, ERROR_SOURCE, INVALID_GLOBALS_TYPE_CODE));
                    }
                } catch (IllegalArgumentException ignored) {
                    // Handled by XSD validation
                }

            }
        }
    }

    private static Diagnostic toDiagnostic(Range range, GlobalsValidationError error) {
        var code = switch (error.code()) {

            case TYPE_MISMATCH -> INVALID_GLOBALS_VAR_TYPE_CODE;
            case VALUE_OUT_OF_RANGE -> INVALID_GLOBALS_VAR_VALUE_CODE;
        };
        return new Diagnostic(range, error.message(), DiagnosticSeverity.Error, ERROR_SOURCE, code);
    }
}
