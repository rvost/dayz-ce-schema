package io.github.rvost.lemminx.dayz.participants.hover;

import io.github.rvost.lemminx.dayz.model.GlobalsModel;
import io.github.rvost.lemminx.dayz.model.GlobalsVariable;
import io.github.rvost.lemminx.dayz.participants.ParticipantsUtils;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.services.extensions.hover.HoverParticipantAdapter;
import org.eclipse.lemminx.services.extensions.hover.IHoverRequest;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

public class GlobalsHoverParticipant extends HoverParticipantAdapter {
    @Override
    public Hover onAttributeValue(IHoverRequest request, CancelChecker cancelChecker) throws Exception {
        var document = request.getXMLDocument();
        if (GlobalsModel.isGlobals(document)) {
            var parent = document.findNodeAt(request.getOffset());
            var attrName = request.getCurrentAttributeName();
            if (GlobalsModel.VALUE_ATTRIBUTE.equals(attrName)) {
                return hoverForValueAttribute(parent, request, cancelChecker);
            }
            if (GlobalsModel.NAME_ATTRIBUTE.equals(attrName)) {
                return null;
            }
        }
        return null;
    }

    private Hover hoverForValueAttribute(DOMNode parent, IHoverRequest request, CancelChecker cancelChecker) {
        var nameAttr = parent.getAttributeNode(GlobalsModel.NAME_ATTRIBUTE);
        if (nameAttr != null) {
            try {
                var variable = GlobalsVariable.valueOf(nameAttr.getValue());
                if (GlobalsModel.TIME_INTERVAL_VARS.contains(variable)) {
                    return ParticipantsUtils.hoverForTimeInterval(request, cancelChecker);
                }
            } catch (IllegalArgumentException ignored) {

            }
        }
        return null;
    }
}
