package io.github.rvost.lemminx.dayz.participants.codeaction;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.commands.ClientCommands;
import io.github.rvost.lemminx.dayz.model.TypesModel;
import io.github.rvost.lemminx.dayz.participants.DOMUtils;
import io.github.rvost.lemminx.dayz.participants.ParticipantsUtils;
import io.github.rvost.lemminx.dayz.participants.diagnostics.TypesDiagnosticsParticipant;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.commons.CodeActionFactory;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionParticipant;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionRequest;
import org.eclipse.lemminx.utils.TextEditUtils;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.*;
import java.util.concurrent.CancellationException;
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
        if (TypesModel.match(document) &&
                ParticipantsUtils.match(diagnostic, TypesDiagnosticsParticipant.LIMITS_CAN_BE_SIMPLIFIED_CODE)) {
            var userFlags = missionService.getUserFlags().inverse();
            tryGetRefactorForFlags(diagnostic, document, userFlags).ifPresent(codeActions::add);
        }
    }

    @Override
    public void doCodeActionUnconditional(ICodeActionRequest request, List<CodeAction> codeActions, CancelChecker cancelChecker) throws CancellationException {
        var document = request.getDocument();
        if (!TypesModel.match(document) || !missionService.isInMissionFolder(document)) {
            return;
        }
        var range = request.getRange();
        var node = ParticipantsUtils.tryGetNodeAtSelection(document, range);
        if (node.isEmpty()) {
            return;
        }
        tryGetInlineUserFlag(document, node.get()).ifPresent(codeActions::add);
        tryGetExtractUserFlag(document, node.get(), range).ifPresent(codeActions::add);
    }

    private Optional<CodeAction> tryGetRefactorForFlags(Diagnostic diagnostic,
                                                        DOMDocument document,
                                                        Map<Set<String>, String> userFlags) {
        var infos = diagnostic.getRelatedInformation();
        var flagNodes = getFLagNodes(infos, document);
        var flags = getFlagValues(flagNodes);
        if (userFlags.containsKey(flags)) {
            var userFlag = userFlags.get(flags);
            var flagType = flagNodes.get(0).getLocalName();
            var edit = computeReplaceEdit(document, flagNodes, flagType, userFlag);
            var we = new WorkspaceEdit(List.of(Either.forLeft(edit)));
            var ca = new CodeAction(String.format("Replace with \"%s\" user flag", userFlag));
            ca.setDiagnostics(List.of(diagnostic));
            ca.setKind(CodeActionKind.QuickFix);
            ca.setEdit(we);
            return Optional.of(ca);
        }

        return Optional.empty();
    }

    private Optional<CodeAction> tryGetInlineUserFlag(DOMDocument document,
                                                      DOMNode node) {
        if (node.hasAttribute(TypesModel.USER_ATTRIBUTE)) {
            var userFlags = missionService.getUserFlags();
            var flags = userFlags.get(node.getAttribute(TypesModel.USER_ATTRIBUTE));
            var flagType = node.getLocalName();
            var insertText = flags.stream()
                    .map(flag -> String.format("<%s %s=\"%s\"/>", flagType, TypesModel.NAME_ATTRIBUTE, flag))
                    .collect(Collectors.joining("\n\t\t"));
            var range = selectNode(node, document);
            var we = CodeActionFactory.getReplaceWorkspaceEdit(insertText, range, document.getTextDocument());
            var ca = new CodeAction("Inline user flag");
            ca.setKind(CodeActionKind.RefactorInline);
            ca.setEdit(we);
            return Optional.of(ca);
        }
        return Optional.empty();
    }

    private Optional<CodeAction> tryGetExtractUserFlag(DOMDocument document, DOMNode node, Range selectedRange) {
        var nodeTag = node.getLocalName();
        if (TypesModel.VALUE_TAG.equals(nodeTag) || TypesModel.USAGE_TAG.equals(nodeTag)) {
            var selectedNodes = DOMUtils.getSiblingsInRange(node, nodeTag, document, selectedRange);
            if (selectedNodes.isEmpty()) {
                return Optional.empty();
            }
            selectedNodes.add(0, node);

            var areValid = selectedNodes.stream().allMatch(n -> n.hasAttribute(TypesModel.NAME_ATTRIBUTE));
            if (!areValid) {
                return Optional.empty();
            }

            var flags = selectedNodes.stream()
                    .map(n -> n.getAttribute(TypesModel.NAME_ATTRIBUTE))
                    .collect(Collectors.toCollection(TreeSet::new));

            var userFlags = missionService.getUserFlags().inverse();
            if (userFlags.containsKey(flags)) {
                return Optional.empty();
            }
            var ca = new CodeAction("Extract user flag");
            ca.setKind(CodeActionKind.RefactorExtract);
            ca.setCommand(new Command("Extract user flag",
                    ClientCommands.APPLY_EXTRACT_USER_FLAG,
                    List.of(nodeTag, flags.stream().toList(), selectedRange))
            );
            return Optional.of(ca);
        }
        return Optional.empty();
    }


    public static TextDocumentEdit computeReplaceEdit(DOMDocument document,
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
        return TextEditUtils.creatTextDocumentEdit(document, edits);
    }

    private static List<DOMNode> getFLagNodes(List<DiagnosticRelatedInformation> infos, DOMDocument document) {
        return infos.stream()
                .map(DiagnosticRelatedInformation::getLocation)
                .map(Location::getRange)
                .map(r -> {
                    try {
                        return document.offsetAt(r.getStart()) + 1;
                    } catch (BadLocationException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(document::findNodeAt)
                .filter(Objects::nonNull)
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
