package io.github.rvost.lemminx.dayz;

import io.github.rvost.lemminx.dayz.model.LimitsDefinitionsModel;
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
    private final Path missionRoot;
    private final Map<String, Set<String>> missionFolders;
    private final Map<String, Set<String>> limitsDefinitions;
    private final Map<String, Set<String>> userLimitsDefinitions;

    private DayzMissionService(Path missionRoot,
                                 Map<String, Set<String>> missionFolders,
                                 Map<String, Set<String>> limitsDefinitions,
                                 Map<String, Set<String>> userLimitsDefinitions) {
        this.missionRoot = missionRoot;
        this.missionFolders = missionFolders;
        this.limitsDefinitions = limitsDefinitions;
        this.userLimitsDefinitions = userLimitsDefinitions;
    }

    public static DayzMissionService create(List<WorkspaceFolder> workspaceFolders) throws URISyntaxException {
        var workspace = workspaceFolders.get(0); //TODO: Handle multiroot workspaces
        var rootUriString = workspace.getUri();
        var rootPath = Path.of(new URI(rootUriString));

        var missionFiles = getMissionFiles(rootPath);
        var limitsDefinitions = LimitsDefinitionsModel.getLimitsDefinitions(rootPath);
        var userLimitsDefinitions = LimitsDefinitionsModel.getUserLimitsDefinitions(rootPath);

        return new DayzMissionService(rootPath, missionFiles, limitsDefinitions, userLimitsDefinitions);
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

    public Map<String, Set<String>> getUserLimitsDefinitions() {
        return userLimitsDefinitions;
    }

    private static Map<String, Set<String>> getMissionFiles(Path path) {
        try {
            return Files.walk(path)
                    .filter(DayzMissionService::isCustomFile)
                    .filter(f -> !f.getParent().getFileName().equals(path.getFileName()))
                    .collect(Collectors.groupingBy(
                            f -> f.getParent().getFileName().toString(),
                            Collectors.mapping(f -> f.getFileName().toString(), Collectors.toCollection(HashSet::new)))
                    );
        } catch (IOException e) {
            return Map.of();
        }
    }

    private static boolean isCustomFile(Path path){
        var fs = FileSystems.getDefault();
        var xmlMatcher = fs.getPathMatcher("glob:**.xml");
        var folderMatcher = fs.getPathMatcher("glob:**/{.*,env}/*.*");

        return xmlMatcher.matches(path) && !folderMatcher.matches(path);
    }

}
