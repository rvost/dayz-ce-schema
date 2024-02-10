package io.github.rvost.lemminx.dayz;

import com.google.common.collect.BiMap;
import io.github.rvost.lemminx.dayz.model.*;
import io.github.rvost.lemminx.dayz.utils.DirWatch;
import io.github.rvost.lemminx.dayz.utils.MissionFolderEvent;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.WorkspaceFolder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO: Refactor
public class DayzMissionService {
    public final Path missionRoot;
    private final ExecutorService executor;
    private final Map<String, Set<String>> missionFolders;
    private volatile Set<String> envFiles;
    private volatile Map<Path, DayzFileType> customFiles;
    private volatile Map<String, Set<String>> limitsDefinitions;
    private volatile Map<String, Set<String>> userLimitsDefinitions;
    private volatile BiMap<String, Set<String>> userFlags;
    private volatile Map<String, Set<String>> randomPresets;
    private volatile Map<String, Range> eventSpawns;
    private volatile Map<String, Range> eventGroups;
    private Map<String, Range> randomPresetsIndex;
    private Map<String, Range> userFlagsIndex;
    private volatile Map<String, Range> rootTypes;
    private volatile Map<String, Range> rootEvents;
    private volatile Map<String, Range> mapGroups;
    private final ConcurrentMap<Path, Map<String, Range>> customTypes = new ConcurrentHashMap<>();
    private final ConcurrentMap<Path, Map<String, Range>> customEvents = new ConcurrentHashMap<>();
    private final DirWatch watch;
    private final ConcurrentLinkedQueue<MissionFolderEvent> folderChangeEvents;
    private final Map<Path, DayzFileType> rootFilePaths;

    private DayzMissionService(Path missionRoot,
                               Map<String, Set<String>> missionFolders,
                               Map<Path, DayzFileType> customFiles, Map<String, Set<String>> limitsDefinitions,
                               Map<String, Set<String>> userLimitsDefinitions,
                               BiMap<String, Set<String>> userFlags,
                               Map<String, Set<String>> randomPresets,
                               Map<String, Range> eventSpawns,
                               Map<String, Range> eventGroups,
                               Map<String, Range> rootTypes,
                               Map<String, Range> rootEvents,
                               Map<String, Range> mapGroups) throws Exception {
        this.missionRoot = missionRoot.toAbsolutePath();
        this.missionFolders = missionFolders;
        this.envFiles = getEnvFiles(missionRoot); // TODO: remove
        this.customFiles = customFiles;
        this.limitsDefinitions = limitsDefinitions;
        this.userLimitsDefinitions = userLimitsDefinitions;
        this.userFlags = userFlags;
        this.randomPresets = randomPresets;
        this.eventSpawns = eventSpawns;
        this.eventGroups = eventGroups;
        this.rootTypes = rootTypes;
        this.rootEvents = rootEvents;
        this.mapGroups = mapGroups;
        this.folderChangeEvents = new ConcurrentLinkedQueue<>();
        this.watch = new DirWatch(missionRoot, this::onMissionFolderEvent);
        this.executor = Executors.newCachedThreadPool();
        rootFilePaths = Map.ofEntries(
                Map.entry(missionRoot.resolve(MissionModel.DB_FOLDER).resolve(TypesModel.TYPES_FILE), DayzFileType.TYPES),
                Map.entry(missionRoot.resolve(SpawnableTypesModel.SPAWNABLETYPES_FILE), DayzFileType.SPAWNABLETYPES),
                Map.entry(missionRoot.resolve(MissionModel.DB_FOLDER).resolve(GlobalsModel.GLOBALS_FILE), DayzFileType.GLOBALS),
                Map.entry(missionRoot.resolve(MissionModel.DB_FOLDER).resolve(EconomyModel.ECONOMY_FILE), DayzFileType.ECONOMY),
                Map.entry(missionRoot.resolve(MissionModel.DB_FOLDER).resolve(EventsModel.EVENTS_FILE), DayzFileType.EVENTS),
                Map.entry(missionRoot.resolve(MissionModel.DB_FOLDER).resolve(MessagesModel.MESSAGES_FILE), DayzFileType.MESSAGES)
        );
    }

    public static DayzMissionService create(List<WorkspaceFolder> workspaceFolders) throws Exception {
        var workspace = workspaceFolders.get(0); //TODO: Handle multiroot workspaces
        var rootUriString = workspace.getUri();
        var rootPath = Path.of(new URI(rootUriString));

        var missionFiles = getMissionFiles(rootPath);
        var customFiles = CfgEconomyCoreModel.getCustomFiles(rootPath);
        var limitsDefinitions = LimitsDefinitionsModel.getLimitsDefinitions(rootPath);
        var userLimitsDefinitions = LimitsDefinitionsModel.getUserLimitsDefinitions(rootPath);
        var userFlags = LimitsDefinitionsModel.getUserFlags(rootPath);
        var randomPresets = RandomPresetsModel.getRandomPresets(rootPath);
        var eventSpawns = CfgEventSpawnsModel.getCfgEventSpawns(rootPath);
        var eventGroups = CfgEventGroupsModel.getCfgEventGroups(rootPath);
        var rootTypes = TypesModel.getRootTypes(rootPath);
        var rootEvents = EventsModel.getRootEvents(rootPath);
        var mapGroups = MapGroupProtoModel.getGroups(rootPath);
        return new DayzMissionService(rootPath, missionFiles, customFiles, limitsDefinitions,
                userLimitsDefinitions, userFlags, randomPresets, eventSpawns, eventGroups,
                rootTypes, rootEvents, mapGroups);
    }

    public void start() {
        executor.execute(watch::processEvents);
        executor.execute(this::refreshCustomTypes);
        executor.execute(this::refreshCustomEvents);
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
        if (path.getFileName().toString().equals(CfgEconomyCoreModel.CFGECONOMYCORE_XML)) {
            var val = CfgEconomyCoreModel.getCustomFiles(missionRoot);
            // TODO: Fix
            if (!val.isEmpty()) {
                customFiles = val;
                refreshCustomTypes();
                refreshCustomEvents();
            }
        }
        if (path.getFileName().toString().equals(LimitsDefinitionsModel.LIMITS_DEFINITION_FILE)) {
            var val = LimitsDefinitionsModel.getLimitsDefinitions(missionRoot);
            if (!val.isEmpty()) {
                limitsDefinitions = val;
            }
        }
        if (path.getFileName().toString().equals(LimitsDefinitionsModel.USER_LIMITS_DEFINITION_FILE)) {
            var definitions = LimitsDefinitionsModel.getUserLimitsDefinitions(missionRoot);
            var index = LimitsDefinitionsModel.getUserFlagsIndex(missionRoot);
            var flags = LimitsDefinitionsModel.getUserFlags(missionRoot);
            if (!definitions.isEmpty()) {
                userLimitsDefinitions = definitions;
                userFlagsIndex = index;
                userFlags = flags;
            }
        }
        if (path.getFileName().toString().equals(RandomPresetsModel.CFGRANDOMPRESETS_FILE)) {
            var presets = RandomPresetsModel.getRandomPresets(missionRoot);
            var index = RandomPresetsModel.getRandomPresetsIndex(missionRoot);
            if (!presets.isEmpty()) {
                randomPresets = presets;
                randomPresetsIndex = index;
            }
        }
        if (path.getFileName().toString().equals(CfgEventSpawnsModel.CFGEVENTSPAWNS_FILE)) {
            var val = CfgEventSpawnsModel.getCfgEventSpawns(missionRoot);
            if (!val.isEmpty()) {
                eventSpawns = val;
            }
        }
        if (path.getFileName().toString().equals(CfgEventGroupsModel.CFGEVENTGROUPS_FILE)) {
            var val = CfgEventGroupsModel.getCfgEventGroups(missionRoot);
            if (!val.isEmpty()) {
                eventGroups = val;
            }
        }
        if (TypesModel.isRootTypes(missionRoot, path)) {
            var val = TypesModel.getRootTypes(missionRoot);
            if (!val.isEmpty()) {
                rootTypes = val;
            }
        }
        if (EventsModel.isRootEvents(missionRoot, path)) {
            var val = EventsModel.getRootEvents(missionRoot);
            if (!val.isEmpty()) {
                rootEvents = val;
            }
        }
        if (path.getFileName().toString().equals(MapGroupProtoModel.MAPGROUPPROTO_FILE)) {
            var val = MapGroupProtoModel.getGroups(missionRoot);
            if (!val.isEmpty()) {
                mapGroups = val;
            }
        }

        var fullPath = path.toAbsolutePath();
        if (customFiles.containsKey(fullPath)) {
            var type = customFiles.get(fullPath);
            switch (type) {
                case TYPES -> updateTypes(fullPath);
                case EVENTS -> updateEvents(fullPath);
                default -> {
                }
            }
        }
    }

    private void onMissionFolderEvent(MissionFolderEvent event) {
        switch (event.type()) {
            case FOLDER_CREATED, FOLDER_DELETED, FILE_CREATED, FILE_DELETED -> {
                folderChangeEvents.add(event);
            }
            case FOLDER_MODIFIED -> {
                var folder = event.path().getFileName().toString();
                if ("env".equals(folder)) {
                    executor.execute(() -> {
                        var newVal = getEnvFiles(missionRoot);
                        if (!newVal.isEmpty()) {
                            envFiles = newVal;
                        }
                    });
                }
            }
            case FILE_MODIFIED -> {
                executor.execute(() -> onFileModified(event.path()));
            }
            default -> {
            }
        }
    }

    private void refreshCustomTypes() {
        var types = customFiles.entrySet().stream()
                .filter(e -> e.getValue().equals(DayzFileType.TYPES))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        customTypes.keySet().retainAll(types.keySet());

        types.keySet().removeAll(customTypes.keySet());
        types.forEach((path, type) -> executor.execute(() -> {
                    var val = TypesModel.getTypes(path);
                    customTypes.putIfAbsent(path, val);
                })
        );
    }

    private void refreshCustomEvents() {
        var events = customFiles.entrySet().stream()
                .filter(e -> e.getValue().equals(DayzFileType.EVENTS))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        customEvents.keySet().retainAll(events.keySet());

        events.keySet().removeAll(customEvents.keySet());
        events.forEach((path, type) -> executor.execute(() -> {
                    var val = EventsModel.getEvents(path);
                    customEvents.putIfAbsent(path, val);
                })
        );
    }

    private void updateTypes(Path path) {
        var val = TypesModel.getTypes(path);
        if (!val.isEmpty()) {
            customTypes.replace(path, val);
        }
    }

    private void updateEvents(Path path) {
        var val = EventsModel.getEvents(path);
        if (!val.isEmpty()) {
            customEvents.replace(path, val);
        }
    }

    public Map<String, Set<String>> getLimitsDefinitions() {
        return limitsDefinitions;
    }

    public Map<String, Set<String>> getUserLimitsDefinitions() {
        return userLimitsDefinitions;
    }

    public BiMap<String, Set<String>> getUserFlags() {
        return userFlags;
    }

    public Set<String> getRootTypes() {
        return rootTypes.keySet();
    }

    public Stream<String> getAllTypes() {
        var custom = customTypes.values().stream().flatMap(m -> m.keySet().stream());
        return Stream.concat(rootTypes.keySet().stream(), custom).distinct();
    }

    public Set<String> getEnvFiles() {
        return envFiles;
    }

    public boolean hasType(String className) {
        return rootTypes.containsKey(className) || customTypes.values().stream().anyMatch(s -> s.containsKey(className));
    }

    public Set<String> getRootEvents() {
        return rootEvents.keySet();
    }

    public Stream<String> getAllEvents() {
        var custom = customEvents.values().stream().flatMap(m -> m.keySet().stream());
        return Stream.concat(rootEvents.keySet().stream(), custom).distinct();
    }

    public boolean hasEvent(String eventName) {
        return rootEvents.containsKey(eventName) || customEvents.values().stream().anyMatch(s -> s.containsKey(eventName));
    }

    public Set<String> getMapGroups() {
        return mapGroups.keySet();
    }

    public Map<String, Range> getMapGroupIndex() {
        return mapGroups;
    }

    public boolean isInMissionFolder(DOMDocument document) {
        try {
            var docPath = Path.of(new URI(document.getDocumentURI())).toAbsolutePath();
            return docPath.startsWith(missionRoot);
        } catch (URISyntaxException ex) {
            return false;
        }
    }

    public boolean isRegistered(Path path) {
        var abs = path.toAbsolutePath();
        return rootFilePaths.containsKey(abs) || customFiles.containsKey(abs);
    }

    public Optional<DayzFileType> getRegisteredType(Path path) {
        var abs = path.toAbsolutePath();
        return Optional.ofNullable(customFiles.get(abs));
    }

    public Stream<Path> getRegisteredFiles(DayzFileType type) {
        var root = rootFilePaths.entrySet().stream();
        var custom = customFiles.entrySet().stream();
        return Stream.concat(root, custom)
                .filter(e -> e.getValue().equals(type))
                .map(Map.Entry::getKey);
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

    private static Set<String> getEnvFiles(Path root) {
        var env = root.resolve("env");
        try (var stream = Files.list(env)) {
            return stream
                    .filter(p -> p.toString().endsWith(".xml"))
                    .map(root::relativize)
                    .map(Path::toString)
                    .map(s -> s.replace('\\', '/'))
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            return Set.of();
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

    public Map<String, Range> getRandomPresetsIndex() {
        if (randomPresetsIndex == null) {
            randomPresetsIndex = RandomPresetsModel.getRandomPresetsIndex(missionRoot);
        }
        return randomPresetsIndex;
    }

    public Map<String, Range> getUserFlagsIndex() {
        if (userFlagsIndex == null) {
            userFlagsIndex = LimitsDefinitionsModel.getUserFlagsIndex(missionRoot);
        }
        return userFlagsIndex;
    }

    public Map<String, Range> getEventSpawns() {
        return eventSpawns;
    }

    public Map<String, Range> getEventGroups() {
        return eventGroups;
    }

    public Map<String, List<Map.Entry<Path, Range>>> getTypesIndex() {
        var rootEntry = Map.entry(missionRoot.resolve(TypesModel.rootTypesPath), rootTypes);
        return Stream.concat(customTypes.entrySet().stream(), Stream.of(rootEntry))
                .flatMap(e -> e.getValue().entrySet().stream()
                        .map(x -> Map.entry(x.getKey(), Map.entry(e.getKey(), x.getValue()))))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));
    }

    public Map<String, List<Map.Entry<Path, Range>>> getEventIndex() {
        var rootEntry = Map.entry(missionRoot.resolve(EventsModel.rootEventsPath), rootEvents);
        return Stream.concat(customEvents.entrySet().stream(), Stream.of(rootEntry))
                .flatMap(e -> e.getValue().entrySet().stream()
                        .map(x -> Map.entry(x.getKey(), Map.entry(e.getKey(), x.getValue()))))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));
    }

    public Map<String, List<Map.Entry<Path, Range>>> getRandomPresetsReferences() {
        return getSpawnableTypesFiles().stream()
                .map(path -> Map.entry(path, SpawnableTypesModel.getPresetsIndex(path)))
                .flatMap(e -> e.getValue().entrySet().stream()
                        .flatMap(x -> x.getValue().stream()
                                .map(r -> Map.entry(x.getKey(), Map.entry(e.getKey(), r))
                                )
                        )
                )
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));

    }

    public Map<String, List<Map.Entry<Path, Range>>> getSpawnableTypesIndex() {
        return getSpawnableTypesFiles().stream()
                .map(path -> Map.entry(path, SpawnableTypesModel.getSpawnableTypes(path)))
                .flatMap(e -> e.getValue().entrySet().stream()
                        .map(x -> Map.entry(x.getKey(), Map.entry(e.getKey(), x.getValue()))))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));
    }

    public List<Path> getTypesFiles() {
        var result = new ArrayList<>(customTypes.keySet());
        result.add(0, missionRoot.resolve(TypesModel.rootTypesPath));
        return result;
    }

    public List<Path> getSpawnableTypesFiles() {
        var result = customFiles.entrySet().stream()
                .filter(p -> p.getValue().equals(DayzFileType.SPAWNABLETYPES))
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(ArrayList::new));
        result.add(0, missionRoot.resolve(SpawnableTypesModel.SPAWNABLETYPES_FILE));
        return result;
    }
}
