package io.github.rvost.lemminx.dayz.utils;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

public class DirWatch {
    private final Path start;
    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private final Consumer<MissionFolderEvent> onEvent;
    private volatile boolean closeWatcherThread = false;

    /**
     * Process all events for keys queued to the watcher
     */
    public void processEvents() {
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

                    var eventType = matchEventKind(kind, child);
                    onEvent.accept(new MissionFolderEvent(eventType, child));

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
    public DirWatch(Path dir, Consumer<MissionFolderEvent> onEvent) throws IOException {
        this.start = dir;
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        this.onEvent = onEvent;
        registerMissionFolder(dir);
    }

    private MissionFolderEvent.EventType matchEventKind(WatchEvent.Kind<?> kind, Path path) {
        if (kind.equals(ENTRY_CREATE)) {
            return Files.isDirectory(path) ? MissionFolderEvent.EventType.FOLDER_CREATED : MissionFolderEvent.EventType.FILE_CREATED;
        } else if (kind.equals(ENTRY_DELETE)) {
            // TODO: Refactor check because Files.isDirectory() returns false for non-existent path
            return Files.isDirectory(path) ? MissionFolderEvent.EventType.FOLDER_DELETED : MissionFolderEvent.EventType.FILE_DELETED;
        } else if (kind.equals(ENTRY_MODIFY)) {
            return Files.isDirectory(path) ? MissionFolderEvent.EventType.FOLDER_MODIFIED : MissionFolderEvent.EventType.FILE_MODIFIED;
        } else {
            return MissionFolderEvent.EventType.UNKNOWN;
        }
    }
}
