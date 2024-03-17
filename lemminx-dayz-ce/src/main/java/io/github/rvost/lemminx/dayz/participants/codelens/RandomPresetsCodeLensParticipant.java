package io.github.rvost.lemminx.dayz.participants.codelens;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.RandomPresetsModel;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lsp4j.Location;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class RandomPresetsCodeLensParticipant extends ReferencesCodeLensParticipant {
    public RandomPresetsCodeLensParticipant(DayzMissionService missionService) {
        super(missionService);
    }

    @Override
    protected boolean match(DOMDocument document) {
        return RandomPresetsModel.match(document);
    }

    @Override
    protected Map<String, List<Location>> getReferencesIndex() {
        return missionService.getRandomPresetsReferences();
    }

    @Override
    protected Stream<DOMAttr> findAttributesForLens(DOMDocument document) {
        return document.getDocumentElement().getChildren().stream()
                .map(n -> n.getAttributeNode(RandomPresetsModel.NAME_ATTRIBUTE))
                .filter(Objects::nonNull);
    }
}
