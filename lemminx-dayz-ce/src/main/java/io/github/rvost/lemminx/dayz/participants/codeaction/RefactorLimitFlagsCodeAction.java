package io.github.rvost.lemminx.dayz.participants.codeaction;

import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.TypesModel;
import io.github.rvost.lemminx.dayz.participants.ParticipantsUtils;
import io.github.rvost.lemminx.dayz.participants.diagnostics.TypesDiagnosticsParticipant;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionParticipant;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionRequest;
import org.eclipse.lemminx.utils.TextEditUtils;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.*;
import java.util.stream.Collectors;

public class RefactorLimitFlagsCodeAction implements ICodeActionParticipant {
    private final DayzMissionService missionService;

    public RefactorLimitFlagsCodeAction(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void doCodeAction(ICodeActionRequest request, List<CodeAction> codeActions, CancelChecker cancelChecker) {
        var diagnostic = request.getDiagnostic();
        var document = request.getDocument();
        if (TypesModel.isTypes(document) &&
                ParticipantsUtils.match(diagnostic, TypesDiagnosticsParticipant.LIMITS_CAN_BE_SIMPLIFIED_CODE)) {
            var range = diagnostic.getRange();
            var parent = ParticipantsUtils.tryGetNodeAtSelection(document, range);
            if (parent.isPresent()) {
                var node = parent.get();
                var userFlags = missionService.getUserFlags().inverse();
                tryGetRefactorForFlags(document, node, TypesModel.VALUE_TAG, userFlags).ifPresent(codeActions::add);
                cancelChecker.checkCanceled();
                tryGetRefactorForFlags(document, node, TypesModel.USAGE_TAG, userFlags).ifPresent(codeActions::add);
            }
        }
    }

    private Optional<CodeAction> tryGetRefactorForFlags(DOMDocument document,
                                                        DOMNode parent,
                                                        String flagType,
                                                        Map<Set<String>, String> userFlags) {
        var fLagNodes = getFLagNodes(parent, flagType);
        var flags = getFlagValues(fLagNodes);
        if (userFlags.containsKey(flags)) {
            var userFlag = userFlags.get(flags);
            var ca = computeCodeAction(document, fLagNodes, flagType, userFlag);
            return Optional.of(ca);
        }

        return Optional.empty();
    }

    private static CodeAction computeCodeAction(DOMDocument document,
                                         List<DOMNode> toRepalce,
                                         String flagType,
                                         String userFlag) {
        var insertText = String.format("<%s %s=\"%s\"/>", flagType, TypesModel.USER_ATTRIBUTE, userFlag);

        var edits = new ArrayList<TextEdit>();
        var firstNode = toRepalce.get(0);
        var replaceEdit = new TextEdit(selectNode(firstNode, document), insertText);
        edits.add(replaceEdit);
        toRepalce.stream()
                .skip(1)
                .map(n -> new TextEdit(selectNode(n, document), ""))
                .forEach(edits::add);
        var docEdit = TextEditUtils.creatTextDocumentEdit(document, edits);

        var we = new WorkspaceEdit(List.of(Either.forLeft(docEdit)));
        var ca = new CodeAction(String.format("Replace with \"%s\" user flag", userFlag));
        ca.setKind(CodeActionKind.QuickFix);
        ca.setEdit(we);
        return ca;
    }


    private static List<DOMNode> getFLagNodes(DOMNode parent, String flagType) {
        return parent.getChildren().stream()
                .filter(n -> flagType.equals(n.getLocalName()))
                .filter(n -> !Strings.isNullOrEmpty(n.getAttribute(TypesModel.NAME_ATTRIBUTE)))
                .toList();
    }

    private static Set<String> getFlagValues(List<DOMNode> nodes) {
        return nodes.stream()
                .map(n -> n.getAttribute(TypesModel.NAME_ATTRIBUTE))
                .collect(Collectors.toSet());
    }

    private static Range selectNode(DOMNode node, DOMDocument document) {
        try {
            var start = document.positionAt(node.getStart());
            var end = document.positionAt(node.getEnd());
            return new Range(start, end);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }
}
