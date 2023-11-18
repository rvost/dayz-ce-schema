package io.github.rvost.lemminx.dayz;

import org.eclipse.lsp4j.WorkspaceFolder;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Map.entry;

public class DayzMissionService {
    private final Path missionRoot;
    private final Map<String, Set<String>> missionFolders;
    private final Map<String, Set<String>> limitsDefinitions;

    protected DayzMissionService(Path missionRoot, Map<String, Set<String>> missionFolders,  Map<String, Set<String>>limitsDefinitions ) {
        this.missionRoot = missionRoot;
        this.missionFolders = missionFolders;
        this.limitsDefinitions =limitsDefinitions;
    }

    public static DayzMissionService create(List<WorkspaceFolder> workspaceFolders) throws URISyntaxException {
        var workspace = workspaceFolders.getFirst(); //TODO: Handle multiroot workspaces
        var rootUriString = workspace.getUri();
        var rootPath = Path.of(new URI(rootUriString));

        var missionFiles = getMissionFiles(rootPath);
        var limitsDefinitions =  getLimitsDefinitions(rootPath);

        return new DayzMissionService(rootPath, missionFiles, limitsDefinitions);
    }

    public Iterable<String> folders() {
        return missionFolders.keySet();
    }

    public Iterable<String> files(String folder) {
        return missionFolders.containsKey(folder) ? missionFolders.get(folder) : Collections.emptyList();
    }

    public boolean hasFolder(String folder) {
        return missionFolders.containsKey(folder);
    }

    public boolean hasFile(String folder, String file) {
        return missionFolders.containsKey(folder) && missionFolders.get(folder).contains(file);
    }

    public Map<String, Set<String>> getLimitsDefinitions() {
        return limitsDefinitions;
    }

    private static Map<String, Set<String>> getMissionFiles(Path path) {
        try {
            var fs = FileSystems.getDefault();
            var xmlMatcher = fs.getPathMatcher("glob:**.xml");
            var folderMatcher = fs.getPathMatcher("glob:**/{.*,env}/*.*");

            return Files.walk(path)
                    .filter(xmlMatcher::matches)
                    .filter(f -> !folderMatcher.matches(f))
                    .filter(f -> !f.getParent().getFileName().equals(path.getFileName()))
                    .collect(Collectors.groupingBy(
                            f -> f.getParent().getFileName().toString(),
                            Collectors.mapping(f -> f.getFileName().toString(), Collectors.toCollection(HashSet::new)))
                    );
        } catch (IOException e) {
            return Map.of();
        }
    }

    private static Map<String, Set<String>> getLimitsDefinitions(Path rootPath) {
        var path = rootPath.resolve("./cfglimitsdefinition.xml");

        try (var input = Files.newInputStream(path)) {
            var db = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
            var doc = db.parse(input);
            if (doc.getDocumentElement() != null) {
                doc.getDocumentElement().normalize();
                var categories = getValues(doc.getElementsByTagName("category"));
                var tags = getValues(doc.getElementsByTagName("tag"));
                var usages = getValues(doc.getElementsByTagName("usage"));
                var values = getValues(doc.getElementsByTagName("value"));
                return new HashMap<>(Map.ofEntries(
                        entry("category", categories),
                        entry("tag", tags),
                        entry("usage", usages),
                        entry("value", values)
                ));
            } else {
                return Map.of();
            }
        } catch (IOException | ParserConfigurationException | SAXException e) {
            return Map.of();
        }
    }

    private static Set<String> getValues(NodeList nodes) {
        var result = new HashSet<String>();
        for (int i = 0; i < nodes.getLength(); i++) {
            var node = nodes.item(i);
            var attributes = node.getAttributes();
            if (attributes != null) {
                var nameAttr = attributes.getNamedItem("name");
                if (nameAttr != null) {
                    result.add(nameAttr.getNodeValue());
                }
            }
        }
        return result;
    }
}
