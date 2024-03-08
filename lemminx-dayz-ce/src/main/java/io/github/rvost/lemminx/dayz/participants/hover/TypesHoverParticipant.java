package io.github.rvost.lemminx.dayz.participants.hover;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.TypesModel;
import io.github.rvost.lemminx.dayz.participants.ParticipantsUtils;
import org.eclipse.lemminx.services.extensions.hover.HoverParticipantAdapter;
import org.eclipse.lemminx.services.extensions.hover.IHoverRequest;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.Map;
import java.util.Set;

public class TypesHoverParticipant extends HoverParticipantAdapter {

    private final DayzMissionService missionService;

    public TypesHoverParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public Hover onAttributeValue(IHoverRequest request, CancelChecker cancelChecker) throws Exception {
        var document = request.getXMLDocument();
        if (TypesModel.match(document) && missionService.isInMissionFolder(document)) {
            var userLimitsDefinitions = missionService.getUserFlags();
            var attrName = request.getCurrentAttributeName();

            if (TypesModel.USER_ATTRIBUTE.equals(attrName)) {
                return hoverForUserFlag(request, userLimitsDefinitions, cancelChecker);
            }
        }
        return null;
    }

    @Override
    public Hover onText(IHoverRequest request, CancelChecker cancelChecker) throws Exception {
        var document = request.getXMLDocument();
        if (TypesModel.match(document)) {
            var parent = request.getParentElement();
            if (TypesModel.TIME_INTERVAL_TAGS.contains(parent.getNodeName())) {
                return ParticipantsUtils.hoverForTimeInterval(request, cancelChecker);
            }
        }
        return null;
    }

    private Hover hoverForUserFlag(IHoverRequest request, Map<String, Set<String>> userFlags, CancelChecker cancelChecker) {
        var supportsMarkdown = request.canSupportMarkupKind(MarkupKind.MARKDOWN);
        var attrValue = request.getCurrentAttribute().getValue();

        if (userFlags.containsKey(attrValue) && !userFlags.get(attrValue).isEmpty()) {
            var options = userFlags.get(attrValue);
            var content = supportsMarkdown ? formatAsMarkdown(options) : formatAsText(options);
            return new Hover(content);
        }
        return null;
    }

    private MarkupContent formatAsMarkdown(Iterable<String> options) {
        var content = "* " + String.join("\n* ", options);
        return new MarkupContent(MarkupKind.MARKDOWN, content);
    }

    private MarkupContent formatAsText(Iterable<String> options) {
        var content = String.join(", ", options);
        return new MarkupContent(MarkupKind.PLAINTEXT, content);
    }

}
