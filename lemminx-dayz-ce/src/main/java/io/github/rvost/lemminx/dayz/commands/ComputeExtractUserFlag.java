package io.github.rvost.lemminx.dayz.commands;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.LimitsDefinitionUserModel;
import io.github.rvost.lemminx.dayz.model.LimitsDefinitionModel;
import io.github.rvost.lemminx.dayz.model.TypesModel;
import io.github.rvost.lemminx.dayz.participants.ParticipantsUtils;
import io.github.rvost.lemminx.dayz.participants.codeaction.RefactorLimitFlagsCodeAction;
import org.eclipse.lemminx.commons.CodeActionFactory;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.services.IXMLDocumentProvider;
import org.eclipse.lemminx.services.extensions.commands.AbstractDOMDocumentCommandHandler;
import org.eclipse.lemminx.services.extensions.commands.ArgumentsUtils;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lemminx.utils.DOMUtils;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ComputeExtractUserFlag extends AbstractDOMDocumentCommandHandler {
    public static final String COMMAND = "dayz-ce-schema.computeExtractUserFlag";

    private final DayzMissionService missionService;

    public ComputeExtractUserFlag(IXMLDocumentProvider documentProvider, DayzMissionService missionService) {
        super(documentProvider);
        this.missionService = missionService;
    }

    @Override
    protected Object executeCommand(DOMDocument document, ExecuteCommandParams params, SharedSettings sharedSettings, CancelChecker cancelChecker) throws Exception {
        var flagType = ArgumentsUtils.getArgAt(params, 1, String.class);
        var flagName = ArgumentsUtils.getArgAt(params, 2, String.class);
        var flags = ArgumentsUtils.getArgAt(params, 3, String[].class);
        var range = ArgumentsUtils.getArgAt(params, 4, Range.class);

        var limitsDoc = resolveUserLimitsDefinitionDocument(document);
        var parentNodeName = (TypesModel.VALUE_TAG.equals(flagType)) ? LimitsDefinitionUserModel.VALUEFLAGS_TAG : LimitsDefinitionUserModel.USAGEFLAGS_TAG;
        var searchResult = limitsDoc.getDocumentElement().getChildren().stream()
                .filter(n -> parentNodeName.equals(n.getLocalName()))
                .findFirst();
        if (searchResult.isEmpty()) {
            return null;
        }
        var parent = searchResult.get();
        var position = limitsDoc.positionAt(parent.getLastChild().getEnd());
        var insertText = formatFlagDefinition(flagType, flagName, flags);
        var limitsEdit = CodeActionFactory.insertEdit(insertText, position, limitsDoc.getTextDocument());

        var node = ParticipantsUtils.tryGetNodeAtSelection(document, range);
        var toReplace = node.map(n -> getSelectedNodes(n, flagType, document, range));
        var sourceEditOpt = toReplace.map(nodes -> RefactorLimitFlagsCodeAction.computeReplaceEdit(document, nodes, flagType, flagName));

        var edits = new ArrayList<TextDocumentEdit>();
        edits.add(limitsEdit);
        sourceEditOpt.ifPresent(edits::add);

        return new WorkspaceEdit(
                edits.stream().map(Either::<TextDocumentEdit, ResourceOperation>forLeft).toList()
        );
    }

    private DOMDocument resolveUserLimitsDefinitionDocument(DOMDocument document) {
        var targetUri = missionService.missionRoot.resolve(LimitsDefinitionUserModel.USER_LIMITS_DEFINITION_FILE).toUri();
        return DOMUtils.loadDocument(
                targetUri.toString(),
                document.getResolverExtensionManager()
        );
    }

    private List<DOMNode> getSelectedNodes(DOMNode firstNode, String nodeTag, DOMDocument document, Range range) {
        var selectedNodes = io.github.rvost.lemminx.dayz.participants.DOMUtils.getSiblingsInRange(firstNode, nodeTag, document, range);
        if (selectedNodes.isEmpty()) {
            return null;
        }
        selectedNodes.add(0, firstNode);
        return selectedNodes;
    }

    private static String formatFlagDefinition(String flagType, String flagName, String[] flags) {
        var body = formatDefinitionBody(flagType, flags);
        return String.format("\n\t\t<%1$s %2$s=\"%3$s\">\n\t\t\t%4$s\n\t\t</%1$s>",
                LimitsDefinitionUserModel.USER_TAG,
                LimitsDefinitionModel.NAME_ATTRIBUTE,
                flagName,
                body);
    }

    private static String formatDefinitionBody(String flagType, String[] flags) {
        return Arrays.stream(flags)
                .map(flag -> String.format("<%s %s=\"%s\"/>", flagType, LimitsDefinitionModel.NAME_ATTRIBUTE, flag))
                .collect(Collectors.joining("\n\t\t\t"));
    }
}
