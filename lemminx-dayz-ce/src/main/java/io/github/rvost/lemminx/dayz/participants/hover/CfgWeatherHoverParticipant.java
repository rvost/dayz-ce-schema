package io.github.rvost.lemminx.dayz.participants.hover;

import io.github.rvost.lemminx.dayz.model.CfgWeatherModel;
import io.github.rvost.lemminx.dayz.participants.ParticipantsUtils;
import org.eclipse.lemminx.services.extensions.hover.HoverParticipantAdapter;
import org.eclipse.lemminx.services.extensions.hover.IHoverRequest;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

public class CfgWeatherHoverParticipant extends HoverParticipantAdapter {
    @Override
    public Hover onAttributeValue(IHoverRequest request, CancelChecker cancelChecker) throws Exception {
        var document = request.getXMLDocument();
        if (CfgWeatherModel.match(document)) {
            var parent = document.findNodeAt(request.getOffset());
            var nodeName = parent.getNodeName();
            var attrName = request.getCurrentAttributeName();

            if (CfgWeatherModel.TIME_INTERVAL_TAGS.contains(nodeName) &&
                    CfgWeatherModel.TIME_INTERVAL_ATTRIBUTES.contains(attrName)) {
                return ParticipantsUtils.hoverForTimeInterval(request, cancelChecker);
            }
        }
        return null;
    }
}
