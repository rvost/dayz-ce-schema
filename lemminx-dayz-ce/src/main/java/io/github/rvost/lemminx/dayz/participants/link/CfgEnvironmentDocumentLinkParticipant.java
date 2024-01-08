package io.github.rvost.lemminx.dayz.participants.link;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.CfgEnvironmentModel;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.IDocumentLinkParticipant;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.DocumentLink;

import java.util.List;
import java.util.Objects;

public class CfgEnvironmentDocumentLinkParticipant implements IDocumentLinkParticipant {
    private final DayzMissionService missionService;

    public CfgEnvironmentDocumentLinkParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void findDocumentLinks(DOMDocument document, List<DocumentLink> links) {
        var territories = document.getDocumentElement().getChildren().get(0);
        territories.getChildren().stream()
                .filter(n -> CfgEnvironmentModel.FILE_TAG.equals(n.getLocalName()))
                .map(n -> n.getAttributeNode(CfgEnvironmentModel.PATH_ATTRIBUTE))
                .filter(Objects::nonNull)
                .map(this::toDocumentLink)
                .forEach(links::add);
    }

    private DocumentLink toDocumentLink(DOMAttr pathAttr) {
        var path = pathAttr.getValue();
        var range = XMLPositionUtility.selectAttributeValue(pathAttr, true);
        var link = missionService.missionRoot.resolve(path).toUri().toString();

        return new DocumentLink(range, link);
    }
}
