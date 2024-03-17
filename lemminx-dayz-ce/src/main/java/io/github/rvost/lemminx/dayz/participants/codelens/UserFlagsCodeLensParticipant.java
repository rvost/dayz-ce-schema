package io.github.rvost.lemminx.dayz.participants.codelens;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.LimitsDefinitionUserModel;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lsp4j.Range;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class UserFlagsCodeLensParticipant extends ReferencesCodeLensParticipant {

    public UserFlagsCodeLensParticipant(DayzMissionService missionService) {
        super(missionService);
    }

    @Override
    protected boolean match(DOMDocument document) {
        return LimitsDefinitionUserModel.match(document);
    }

    @Override
    protected Map<String, List<Map.Entry<Path, Range>>> getReferencesIndex() {
        return missionService.getUserFlagReferences();
    }

    @Override
    protected Stream<DOMAttr> findAttributesForLens(DOMDocument document) {
        return findAttributesInChildrenByName(document, LimitsDefinitionUserModel.NAME_ATTRIBUTE);
    }
}
