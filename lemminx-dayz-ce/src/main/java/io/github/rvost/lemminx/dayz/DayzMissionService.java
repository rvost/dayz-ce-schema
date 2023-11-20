package io.github.rvost.lemminx.dayz;

import io.github.rvost.lemminx.dayz.model.LimitsDefinitionsModel;
import io.github.rvost.lemminx.dayz.model.RandomPresetsModel;
import org.eclipse.lsp4j.WorkspaceFolder;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;

public class DayzMissionService {
    private final Path missionRoot;
    private final Map<String, Set<String>> missionFolders;
    private volatile Map<String, Set<String>> limitsDefinitions;
    private volatile Map<String, Set<String>> userLimitsDefinitions;
    private volatile Map<String, Set<String>> randomPresets;
    private final DirWatch watch;
    private final ConcurrentLinkedQueue<MissionFolderEvent> folderChangeEvents;
    private final ConcurrentLinkedQueue<Path> fileModifiedEvents;

    private DayzMissionService(Path missionRoot,
                               Map<String, Set<String>> missionFolders,
                               Map<String, Set<String>> limitsDefinitions,
                               Map<String, Set<String>> userLimitsDefinitions,
                               Map<String, Set<String>> randomPresets
    ) throws Exception {
        this.missionRoot = missionRoot;
        this.missionFolders = missionFolders;
        this.limitsDefinitions = limitsDefinitions;
        this.userLimitsDefinitions = userLimitsDefinitions;
        this.randomPresets = randomPresets;
        this.folderChangeEvents = new ConcurrentLinkedQueue<>();
        this.fileModifiedEvents = new ConcurrentLinkedQueue<>();
        this.watch = DirWatch.watchDirectory(missionRoot, this.folderChangeEvents, this.fileModifiedEvents);
    }

    public static DayzMissionService create(List<WorkspaceFolder> workspaceFolders) throws Exception {
        var workspace = workspaceFolders.get(0); //TODO: Handle multiroot workspaces
        var rootUriString = workspace.getUri();
        var rootPath = Path.of(new URI(rootUriString));

        var missionFiles = getMissionFiles(rootPath);
        var limitsDefinitions = LimitsDefinitionsModel.getLimitsDefinitions(rootPath);
        var userLimitsDefinitions = LimitsDefinitionsModel.getUserLimitsDefinitions(rootPath);
        var randomPresets = RandomPresetsModel.getRandomPresets(rootPath);
        var service = new DayzMissionService(rootPath, missionFiles, limitsDefinitions, userLimitsDefinitions, randomPresets);
        new Thread(service::watchModifiedFiles).start();
        return service;
    }

    public void close() {
        watch.stop();
    }

    public synchronized Map<String, Set<String>> getMissionFolders() {
        applyEvents();
        return missionFolders;
    }

    private void applyEvents() {
        for (var event : folderChangeEvents) {
            var name = event.path().getFileName().toString();
            switch (event.type()) {
                case FOLDER_CREATED -> {
                    missionFolders.put(name, new HashSet<>());
                }
                case FOLDER_DELETED -> {
                    missionFolders.remove(name);
                }
                case FILE_CREATED -> {
                    var parent = event.path().getParent().getFileName().toString();
                    if (missionFolders.containsKey(parent)) {
                        missionFolders.get(parent).add(name);
                    }
                }
                case FILE_DELETED -> {
                    var parent = event.path().getParent().getFileName().toString();
                    if (missionFolders.containsKey(parent)) {
                        missionFolders.get(parent).remove(name);
                    }
                }
            }
        }
    }

    private void watchModifiedFiles() {
        while (true) {
            var path = fileModifiedEvents.poll();
            if (path != null) {
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
            }
        }
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

enum MissionFolderEventType {
    FOLDER_CREATED,
    FOLDER_DELETED,
    FILE_CREATED,
    FILE_DELETED
}

record MissionFolderEvent(MissionFolderEventType type, Path path) {
}

class DirWatch {
    private final Path start;
    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private final ConcurrentLinkedQueue<MissionFolderEvent> folderEvents;
    private final ConcurrentLinkedQueue<Path> fileModifiedEvents;
    private volatile boolean closeWatcherThread;

    /**
     * Process all events for keys queued to the watcher
     */
    private void processEvents() {
        while (!closeWatcherThread) {
            try {
                // wait for key to be signalled
                var key = watcher.take();


                Path dir = keys.get(key);
                if (dir == null) {
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    var kind = event.kind();

                    if (kind == OVERFLOW) {
                        continue;
                    }

                    // Context for directory entry event is the file name of entry
                    WatchEvent<Path> ev = cast(event);
                    Path name = ev.context();
                    Path child = dir.resolve(name);

                    handleEvent(kind, child);

                    if (kind.equals(ENTRY_CREATE)) {
                        try {
                            if (Files.isDirectory(child, NOFOLLOW_LINKS) && child.getParent().equals(start)) {
                                register(child);
                            }
                        } catch (IOException ignored) {
                        }
                    }
                }
                // reset key and remove from set if directory no longer accessible
                var valid = key.reset();
                if (!valid) {
                    keys.remove(key);
                    // all directories are inaccessible
                    if (keys.isEmpty()) {
                        break;
                    }
                }

            } catch (InterruptedException ignored) {

            } catch (ClosedWatchServiceException e) {
                break;
            }

        }
        closeWatcherThread = true;
        System.out.println("DirWatcherThread exited.");
    }


    public static DirWatch watchDirectory(Path dir, ConcurrentLinkedQueue<MissionFolderEvent> folderEventsQueue,
                                          ConcurrentLinkedQueue<Path> fileModifiedEventsQueue) throws Exception {
        final DirWatch watchDir = new DirWatch(dir, folderEventsQueue, fileModifiedEventsQueue);
        watchDir.closeWatcherThread = false;
        new Thread(watchDir::processEvents, "DirWatcherThread").start();
        return watchDir;
    }

    public void stop() {
        try {
            watcher.close();
        } catch (IOException ioe) {
        }
        closeWatcherThread = true;
    }

    @SuppressWarnings("unchecked")
    private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and its subdirectories, with the
     * WatchService.
     */
    private void registerMissionFolder(final Path start) throws IOException {
        // register directory and first level subdirectories
        try (var dirs = Files.walk(start, 1).filter(Files::isDirectory)) {
            for (var dir : dirs.toList()) {
                register(dir);
            }
        }
    }

    /**
     * Creates a WatchService and registers the given directory
     */
    private DirWatch(Path dir,
                     ConcurrentLinkedQueue<MissionFolderEvent> folderEventsQueue,
                     ConcurrentLinkedQueue<Path> fileModifiedEventsQueue) throws IOException {
        this.start = dir;
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        this.folderEvents = folderEventsQueue;
        this.fileModifiedEvents = fileModifiedEventsQueue;
        registerMissionFolder(dir);
    }

    private void handleEvent(WatchEvent.Kind<?> kind, Path path) {
        if (kind.equals(ENTRY_CREATE)) {
            if (Files.isDirectory(path)) {
                folderEvents.add(new MissionFolderEvent(MissionFolderEventType.FOLDER_CREATED, path));
            } else if (DayzMissionService.isCustomFile(path)) {
                folderEvents.add(new MissionFolderEvent(MissionFolderEventType.FILE_CREATED, path));
            }
        }
        if (kind.equals(ENTRY_DELETE)) {
            if (Files.isDirectory(path)) {
                folderEvents.add(new MissionFolderEvent(MissionFolderEventType.FOLDER_DELETED, path));
            } else if (DayzMissionService.isCustomFile(path)) {
                folderEvents.add(new MissionFolderEvent(MissionFolderEventType.FILE_DELETED, path));
            }
        }
        if (kind.equals(ENTRY_MODIFY) && !Files.isDirectory(path)) {
            fileModifiedEvents.add(path);
        }
    }
}