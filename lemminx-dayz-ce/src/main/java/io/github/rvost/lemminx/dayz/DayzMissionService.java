package io.github.rvost.lemminx.dayz;

import org.eclipse.lsp4j.WorkspaceFolder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class DayzMissionService {
    private final Map<String, Set<String>> missionFolders;

    protected DayzMissionService(Map<String, Set<String>> missionFolders) {
        this.missionFolders = missionFolders;
    }

    public static DayzMissionService create(List<WorkspaceFolder> workspaceFolders) {
        var workspace = workspaceFolders.getFirst(); //TODO: Handle multiroot workspaces
        var rootUriString = workspace.getUri();
        var mapping = getMissionFiles(rootUriString);

        return new DayzMissionService(mapping);
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

    private static Map<String, Set<String>> getMissionFiles(String uriString) {
        try {
            var path = Path.of(new URI(uriString));
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
        } catch (URISyntaxException | IOException e) {
            return Map.of();
        }
    }
}
