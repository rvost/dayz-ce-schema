package io.github.rvost.lemminx.dayz.participants.link;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.CfgEconomyCoreModel;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.IDocumentLinkParticipant;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.DocumentLink;

import java.util.List;
import java.util.Objects;

public class CfgEconomyCoreDocumentLinkParticipant implements IDocumentLinkParticipant {

    private final DayzMissionService missionService;

    public CfgEconomyCoreDocumentLinkParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void findDocumentLinks(DOMDocument document, List<DocumentLink> links) {
        if (CfgEconomyCoreModel.isCfgEconomyCore(document)) {
            document.getDocumentElement().getChildren().stream()
                    .filter(n -> CfgEconomyCoreModel.CE_TAG.equals(n.getLocalName()))
                    .filter(n -> n.hasAttribute(CfgEconomyCoreModel.FOLDER_ATTRIBUTE))
                    .flatMap(n -> n.getChildren().stream()
                            .map(x -> x.getAttributeNode(CfgEconomyCoreModel.NAME_ATTRIBUTE))
                            .filter(Objects::nonNull)
                            .map(attr -> new ImmutablePair<>(n.getAttribute(CfgEconomyCoreModel.FOLDER_ATTRIBUTE), attr))
                    )
                    .map(this::toDocumentLink)
                    .forEach(links::add);
        }
    }

    private DocumentLink toDocumentLink(Pair<String, DOMAttr> pair) {
        var folder = pair.getLeft();
        var fileAttr = pair.getRight();
        var file = fileAttr.getValue();
        var range = XMLPositionUtility.selectAttributeValue(fileAttr, true);
        var link = missionService.missionRoot.resolve(folder).resolve(file).toUri().toString();

        return new DocumentLink(range, link);
    }
}
