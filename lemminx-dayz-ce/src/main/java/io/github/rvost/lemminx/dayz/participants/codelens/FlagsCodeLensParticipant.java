package io.github.rvost.lemminx.dayz.participants.codelens;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.LimitsDefinitionsModel;
import org.eclipse.lemminx.client.ClientCommands;
import org.eclipse.lemminx.services.extensions.codelens.ICodeLensParticipant;
import org.eclipse.lemminx.services.extensions.codelens.ICodeLensRequest;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.List;
import java.util.Objects;

public class FlagsCodeLensParticipant implements ICodeLensParticipant {
    private static final String LABEL = "usage";
    private final DayzMissionService missionService;

    public FlagsCodeLensParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void doCodeLens(ICodeLensRequest request, List<CodeLens> codeLens, CancelChecker cancelChecker) {
        var document = request.getDocument();
        if (!LimitsDefinitionsModel.isLimitsDefinitions(document)) {
            return;
        }
        var references = missionService.getFlagReferences();
        document.getDocumentElement().getChildren().stream()
                .flatMap(n -> n.getChildren().stream())
                .map(n -> n.getAttributeNode(LimitsDefinitionsModel.NAME_ATTRIBUTE))
                .filter(Objects::nonNull)
                .map(attr -> {
                    var range = XMLPositionUtility.createRange(attr);
                    var lens = new CodeLens(range);
                    var size = references.getOrDefault(attr.getValue(), List.of()).size();
                    var command = new Command(size + " " + LABEL + ((size == 1) ? "" : "s"),
                            ClientCommands.SHOW_REFERENCES,
                            List.of(document.getDocumentURI(), range.getStart()));
                    lens.setCommand(command);
                    return lens;
                })
                .forEach(codeLens::add);
    }
}
