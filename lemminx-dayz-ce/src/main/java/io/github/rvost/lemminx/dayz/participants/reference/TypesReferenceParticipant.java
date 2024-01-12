package io.github.rvost.lemminx.dayz.participants.reference;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.TypesModel;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.services.extensions.AbstractReferenceParticipant;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

public class TypesReferenceParticipant extends AbstractReferenceParticipant {
    private final DayzMissionService missionService;

    public TypesReferenceParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    protected boolean match(DOMDocument document) {
        return TypesModel.isTypes(document);
    }

    @Override
    protected void findReferences(DOMNode node, Position position, int offset, ReferenceContext referenceContext,
                                  List<Location> locations, CancelChecker cancelChecker) {
        if (TypesModel.TYPE_TAG.equals(node.getLocalName())) {
            DOMAttr attr = node.findAttrAt(offset);
            if (attr != null && TypesModel.NAME_ATTRIBUTE.equals(attr.getName())) {
                var type = attr.getValue();
                provideOtherFileReferences(type, node.getOwnerDocument(), locations);
                cancelChecker.checkCanceled();
                provideSpawnableTypesReferences(type, locations);
            }
        }
    }

    private void provideSpawnableTypesReferences(String type, List<Location> locations) {
        var index = missionService.getSpawnableTypesIndex();
        if(index.containsKey(type)){
            var options = index.get(type);
            options.stream()
                    .map(e -> new Location(e.getKey().toUri().toString(), e.getValue()))
                    .forEach(locations::add);
        }
    }

    private void provideOtherFileReferences(String type, DOMDocument document, List<Location> locations) {
        var index = missionService.getTypesIndex();
        if (index.containsKey(type)) {
            try {
                var docUri = new URI(document.getDocumentURI());
                var options = index.get(type);
                options.stream()
                        .filter(e -> !e.getKey().equals(Path.of(docUri)))
                        .forEach(e -> locations.add(new Location(e.getKey().toUri().toString(), e.getValue())));
            } catch (URISyntaxException ignored) {

            }
        }
    }
}
