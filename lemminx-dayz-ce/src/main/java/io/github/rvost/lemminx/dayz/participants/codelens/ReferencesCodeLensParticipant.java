package io.github.rvost.lemminx.dayz.participants.codelens;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import org.eclipse.lemminx.client.ClientCommands;
import org.eclipse.lemminx.client.CodeLensKind;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.codelens.ICodeLensParticipant;
import org.eclipse.lemminx.services.extensions.codelens.ICodeLensRequest;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class ReferencesCodeLensParticipant implements ICodeLensParticipant {
    protected final DayzMissionService missionService;

    protected ReferencesCodeLensParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    protected abstract boolean match(DOMDocument document);

    protected abstract Map<String, List<Location>> getReferencesIndex();

    protected abstract Stream<DOMAttr> findAttributesForLens(DOMDocument document);

    @Override
    public void doCodeLens(ICodeLensRequest request, List<CodeLens> codeLens, CancelChecker cancelChecker) {
        var document = request.getDocument();
        var supportedByClient = request.isSupportedByClient(CodeLensKind.References);
        if (!match(document)) {
            return;
        }
        var references = getReferencesIndex();
        findAttributesForLens(document)
                .map(attr -> {
                    var range = XMLPositionUtility.createRange(attr);
                    var lens = new CodeLens(range);
                    var size = references.getOrDefault(attr.getValue(), List.of()).size();
                    var command = new ReferenceCommand(document.getDocumentURI(), range.getStart(), supportedByClient);
                    command.setReferences(size);
                    lens.setCommand(command);
                    return lens;
                })
                .forEach(codeLens::add);
    }

    protected static Stream<DOMAttr> findAttributesInChildrenByName(DOMDocument document, String attribute) {
        return document.getDocumentElement().getChildren().stream()
                .flatMap(n -> n.getChildren().stream())
                .map(n -> n.getAttributeNode(attribute))
                .filter(Objects::nonNull);
    }

    protected static class ReferenceCommand extends Command {

        private transient int nbReferences = 0;

        public ReferenceCommand(String uri, Position position, boolean supportedByClient) {
            super(computeTitle(0), supportedByClient ? ClientCommands.SHOW_REFERENCES : "");
            super.setArguments(Arrays.asList(uri, position));
        }

        public void setReferences(int count) {
            nbReferences = count;
            super.setTitle(computeTitle(nbReferences));
        }

        private static String computeTitle(int nbReferences) {
            if (nbReferences == 1) {
                return nbReferences + " reference";
            }
            return nbReferences + " references";
        }

    }
}
