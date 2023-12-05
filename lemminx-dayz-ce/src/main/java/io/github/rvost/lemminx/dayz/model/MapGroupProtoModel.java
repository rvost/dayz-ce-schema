package io.github.rvost.lemminx.dayz.model;

import org.eclipse.lemminx.dom.DOMDocument;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class MapGroupProtoModel {
    public static final String MAPGROUPPROTO_FILE = "mapgroupproto.xml";
    public static final String DEFAULT_TAG = "default";
    public static final String GROUP_TAG = "group";
    public static final String CONTAINER_TAG = "container";

    public static final String NAME_ATTRIBUTE = "name";
    public static final String USER_ATTRIBUTE = "user";
    public static final String DE_ATTRIBUTE = "de";

    public static boolean isMapGroupProto(DOMDocument document) {
        if (document == null) {
            return false;
        }
        var uri = document.getDocumentURI();
        return uri != null && uri.toLowerCase().endsWith(MAPGROUPPROTO_FILE);
    }

    public static Set<String> getGroups(Path missionPath) {
        var path = missionPath.resolve(MAPGROUPPROTO_FILE);

        try (var input = Files.newInputStream(path)) {
            return getGroups(input);
        } catch (IOException e) {
            return Set.of();
        }
    }

    private static Set<String> getGroups(InputStream input) throws IOException {
        try {
            var db = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
            var doc = db.parse(input);
            if (doc.getDocumentElement() != null) {
                doc.getDocumentElement().normalize();
                return getGroupNames(doc.getElementsByTagName(GROUP_TAG));
            } else {
                return Set.of();
            }

        } catch (ParserConfigurationException | SAXException e) {
            return Set.of();
        }
    }

    private static Set<String> getGroupNames(NodeList nodes) {
        var result = new HashSet<String>();
        for (int i = 0; i < nodes.getLength(); i++) {
            var node = nodes.item(i);
            var attributes = node.getAttributes();
            if (attributes != null) {
                var nameAttr = attributes.getNamedItem(NAME_ATTRIBUTE);
                if (nameAttr != null) {
                    result.add(nameAttr.getNodeValue());
                }
            }
        }
        return result;
    }
}
