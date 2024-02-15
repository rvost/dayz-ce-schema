package io.github.rvost.lemminx.dayz.participants.codelens;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.RandomPresetsModel;
import org.eclipse.lemminx.client.ClientCommands;
import org.eclipse.lemminx.services.extensions.codelens.ICodeLensParticipant;
import org.eclipse.lemminx.services.extensions.codelens.ICodeLensRequest;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.List;
import java.util.Objects;

public class RandomPresetsCodeLensParticipant implements ICodeLensParticipant {
    private static final String LABEL = "reference";
    private final DayzMissionService missionService;

    public RandomPresetsCodeLensParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void doCodeLens(ICodeLensRequest request, List<CodeLens> codeLens, CancelChecker cancelChecker) {
        var document = request.getDocument();
        if (!RandomPresetsModel.isRandomPresets(document)) {
            return;
        }
        var references = missionService.getRandomPresetsReferences();
        document.getDocumentElement().getChildren().stream()
                .map(n -> n.getAttributeNode(RandomPresetsModel.NAME_ATTRIBUTE))
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
