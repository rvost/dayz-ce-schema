package io.github.rvost.lemminx.dayz.participants.codelens;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.LimitsDefinitionModel;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lsp4j.Location;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class FlagsCodeLensParticipant extends ReferencesCodeLensParticipant {
    public FlagsCodeLensParticipant(DayzMissionService missionService) {
        super(missionService);
    }

    @Override
    protected boolean match(DOMDocument document) {
        return LimitsDefinitionModel.match(document);
    }

    @Override
    protected Map<String, List<Location>> getReferencesIndex() {
        return missionService.getFlagReferences();
    }

    @Override
    protected Stream<DOMAttr> findAttributesForLens(DOMDocument document) {
        return findAttributesInChildrenByName(document, LimitsDefinitionModel.NAME_ATTRIBUTE);
    }
}
