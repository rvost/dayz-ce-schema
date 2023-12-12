package io.github.rvost.lemminx.dayz;

import io.github.rvost.lemminx.dayz.model.DayzFileType;
import io.github.rvost.lemminx.dayz.model.MissionModel;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.eclipse.lemminx.services.IXMLDocumentProvider;
import org.eclipse.lemminx.uriresolver.URIResolverExtension;

import java.io.IOException;

public class DayzSchemaURIResolver implements URIResolverExtension {
    private final IXMLDocumentProvider documentProvider;

    public DayzSchemaURIResolver(IXMLDocumentProvider documentProvider) {
        this.documentProvider = documentProvider;
    }

    @Override
    public String resolve(String baseLocation, String publicId, String systemId) {
        if (systemId != null) {
            return null;
        }
        var doc = documentProvider.getDocument(baseLocation);
        if (doc == null) {
            return null;
        }
        var type = MissionModel.TryGetFileType(doc);

        return type.map(DayzSchemaURIResolver::getTypeSchemaURI).orElse(null);

    }

    @Override
    public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) throws XNIException, IOException {
        var publicId = resourceIdentifier.getNamespace();
        var baseLocation = resourceIdentifier.getBaseSystemId();
        var xsd = resolve(baseLocation, publicId, null);
        if (xsd != null) {
            return new XMLInputSource(publicId, xsd, xsd);
        }
        return null;
    }

    // TODO: Make configurable
    private static String getTypeSchemaURI(DayzFileType type) {
        return switch (type) {
            case TYPES -> "https://rvost.github.io/DayZ-Central-Economy-Schema/db/types.xsd";
            case SPAWNABLETYPES -> "https://rvost.github.io/DayZ-Central-Economy-Schema/cfgspawnabletypes.xsd";
            case GLOBALS -> "https://rvost.github.io/DayZ-Central-Economy-Schema/db/globals.xsd";
            case ECONOMY -> "https://rvost.github.io/DayZ-Central-Economy-Schema/db/economy.xsd";
            case EVENTS -> "https://rvost.github.io/DayZ-Central-Economy-Schema/db/events.xsd";
            case MESSAGES -> "https://rvost.github.io/DayZ-Central-Economy-Schema/db/messages.xsd";
        };
    }

}
