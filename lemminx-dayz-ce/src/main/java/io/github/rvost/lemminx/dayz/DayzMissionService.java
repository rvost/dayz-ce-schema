package io.github.rvost.lemminx.dayz;

import io.github.rvost.lemminx.dayz.model.LimitsDefinitionsModel;
import io.github.rvost.lemminx.dayz.model.RandomPresetsModel;
import io.github.rvost.lemminx.dayz.model.TypesModel;
import io.github.rvost.lemminx.dayz.utils.DirWatch;
import io.github.rvost.lemminx.dayz.utils.MissionFolderEvent;
import org.eclipse.lsp4j.WorkspaceFolder;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DayzMissionService {
    public final Path missionRoot;
    private final ExecutorService executor;
    private final Map<String, Set<String>> missionFolders;
    private volatile Map<String, Set<String>> limitsDefinitions;
    private volatile Map<String, Set<String>> userLimitsDefinitions;
    private volatile Map<String, Set<String>> randomPresets;
    private volatile Set<String> rootTypes;
    private final DirWatch watch;
    private final ConcurrentLinkedQueue<MissionFolderEvent> folderChangeEvents;

    private DayzMissionService(Path missionRoot,
                               Map<String, Set<String>> missionFolders,
                               Map<String, Set<String>> limitsDefinitions,
                               Map<String, Set<String>> userLimitsDefinitions,
                               Map<String, Set<String>> randomPresets,
                               Set<String> rootTypes) throws Exception {
        this.missionRoot = missionRoot;
        this.missionFolders = missionFolders;
        this.limitsDefinitions = limitsDefinitions;
        this.userLimitsDefinitions = userLimitsDefinitions;
        this.randomPresets = randomPresets;
        this.rootTypes = rootTypes;
        this.folderChangeEvents = new ConcurrentLinkedQueue<>();
        this.watch = new DirWatch(missionRoot, this::onMissionFolderEvent);
        this.executor = Executors.newCachedThreadPool();
    }

    public static DayzMissionService create(List<WorkspaceFolder> workspaceFolders) throws Exception {
        var workspace = workspaceFolders.get(0); //TODO: Handle multiroot workspaces
        var rootUriString = workspace.getUri();
        var rootPath = Path.of(new URI(rootUriString));

        var missionFiles = getMissionFiles(rootPath);
        var limitsDefinitions = LimitsDefinitionsModel.getLimitsDefinitions(rootPath);
        var userLimitsDefinitions = LimitsDefinitionsModel.getUserLimitsDefinitions(rootPath);
        var randomPresets = RandomPresetsModel.getRandomPresets(rootPath);
        var rootTypes = TypesModel.getRootTypes(rootPath);
        return new DayzMissionService(rootPath, missionFiles, limitsDefinitions, userLimitsDefinitions, randomPresets, rootTypes);
    }

    public void start() {
        executor.execute(watch::processEvents);
    }

    public void close() {
        watch.stop();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }

    public synchronized Map<String, Set<String>> getMissionFolders() {
        applyEvents();
        return missionFolders;
    }

    private void applyEvents() {
        for (var event = folderChangeEvents.poll(); event != null; event = folderChangeEvents.poll()) {
            var name = event.path().getFileName().toString();
            switch (event.type()) {
                case FOLDER_CREATED -> {
                    missionFolders.put(name, new HashSet<>());
                }
                case FOLDER_DELETED -> {
                    missionFolders.remove(name);
                }
                case FILE_CREATED -> {
                    var parent = event.path().getParent();
                    var parentName = parent.getFileName().toString();
                    if (missionFolders.containsKey(parentName)) {
                        missionFolders.get(parentName).add(name);
                    } else if (isCustomFile(event.path()) && parent.getParent().equals(missionRoot)) {
                        missionFolders.put(parentName, new HashSet<>());
                        missionFolders.get(parentName).add(name);
                    }
                }
                case FILE_DELETED -> {
                    var parent = event.path().getParent().getFileName().toString();
                    if (missionFolders.containsKey(parent)) {
                        missionFolders.get(parent).remove(name);
                    } else if (parent.equals(missionRoot.getFileName().toString())) {
                        missionFolders.remove(name); // TODO: Refactor with FOLDER_DELETED
                    }
                }
            }
        }
    }

    private void onFileModified(Path path) {
        if (path.getFileName().toString().equals(LimitsDefinitionsModel.LIMITS_DEFINITION_FILE)) {
            var val = LimitsDefinitionsModel.getLimitsDefinitions(missionRoot);
            if (!val.isEmpty()) {
                limitsDefinitions = val;
            }
        }
        if (path.getFileName().toString().equals(LimitsDefinitionsModel.USER_LIMITS_DEFINITION_FILE)) {
            var val = LimitsDefinitionsModel.getUserLimitsDefinitions(missionRoot);
            if (!val.isEmpty()) {
                userLimitsDefinitions = val;
            }
        }
        if (path.getFileName().toString().equals(RandomPresetsModel.CFGRANDOMPRESETS_FILE)) {
            var val = RandomPresetsModel.getRandomPresets(missionRoot);
            if (!val.isEmpty()) {
                randomPresets = val;
            }
        }
        if (TypesModel.isRootTypes(missionRoot, path)) {
            var val = TypesModel.getRootTypes(missionRoot);
            if (!val.isEmpty()) {
                rootTypes = val;
            }
        }
    }

    private void onMissionFolderEvent(MissionFolderEvent event) {
        switch (event.type()) {
            case FOLDER_CREATED, FOLDER_DELETED, FILE_CREATED, FILE_DELETED -> {
                folderChangeEvents.add(event);
            }
            case FILE_MODIFIED -> {
                executor.execute(() -> onFileModified(event.path()));
            }
            default -> {
            }
        }
    }

    public Map<String, Set<String>> getLimitsDefinitions() {
        return limitsDefinitions;
    }

    public Map<String, Set<String>> getUserLimitsDefinitions() {
        return userLimitsDefinitions;
    }

    public Set<String> getRootTypes() {
        return rootTypes;
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

    static boolean isCustomFile(Path path) {
        var fs = FileSystems.getDefault();
        var xmlMatcher = fs.getPathMatcher("glob:**.xml");
        var folderMatcher = fs.getPathMatcher("glob:**/{.*,env}/*.*");

        return xmlMatcher.matches(path) && !folderMatcher.matches(path);
    }

    public Map<String, Set<String>> getRandomPresets() {
        return randomPresets;
    }
}
