package ratismal.drivebackup.config.configSections;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ratismal.drivebackup.config.configSections.BackupList.BackupListEntry.BackupLocation;
import ratismal.drivebackup.util.FileUtil;
import ratismal.drivebackup.util.LocalDateTimeFormatter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Deprecated
public class BackupList {
    
    public static final String ENTRY = "entry";
    
    @Deprecated
    public static class BackupListEntry {
        @Deprecated
        public interface BackupLocation {
            List<Path> getPaths();
            String toString();
        }

        @Deprecated
        public static class PathBackupLocation implements BackupLocation {
            private final Path path;

            public PathBackupLocation(String path) {
                this.path = Paths.get(path);
            }

            public List<Path> getPaths() {
                return Collections.singletonList(path);
            }

            public String toString() {
                return path.toString();
            }
        }

        @Deprecated
        public static class GlobBackupLocation implements BackupLocation {
            private final String glob;

            public GlobBackupLocation(String glob) {
                this.glob = glob;
            }

            public List<Path> getPaths() {
                return FileUtil.generateGlobFolderList(glob, ".");
            }

            public String toString() {
                return glob;
            }
        }

        public final BackupLocation location;
        public final LocalDateTimeFormatter formatter;
        public final boolean create;
        public final String[] blacklist;
        
        @Contract (pure = true)
        public BackupListEntry(
            BackupLocation location,
            LocalDateTimeFormatter formatter, 
            boolean create, 
            String[] blacklist
            ) {

            this.location = location;
            this.formatter = formatter;
            this.create = create;
            this.blacklist = blacklist;
        }
    }

    public final BackupListEntry[] list;

    @Contract (pure = true)
    public BackupList(
        BackupListEntry[] list
        ) {

        this.list = list;
    }

    @NotNull
    @Contract ("_ -> new")
    public static BackupList parse(@NotNull FileConfiguration config) {
        List<Map<?, ?>> rawList = config.getMapList("backup-list");
        ArrayList<BackupListEntry> list = new ArrayList<>();
        for (Map<?, ?> rawListEntry : rawList) {
            String entryIndex = String.valueOf(rawList.indexOf(rawListEntry) + 1);
            BackupLocation location;
            if (rawListEntry.containsKey("glob")) {
                try {
                    location = new BackupListEntry.GlobBackupLocation((String) rawListEntry.get("glob"));
                } catch (ClassCastException e) {
                    continue;
                }
            } else if (rawListEntry.containsKey("path")) {
                try {
                    location = new BackupListEntry.PathBackupLocation((String) rawListEntry.get("path"));
                } catch (ClassCastException e) {
                    continue;
                }
            } else {
                continue;
            }
            LocalDateTimeFormatter formatter;
            try {
                formatter = LocalDateTimeFormatter.ofPattern(null,(String) rawListEntry.get("format"));
            } catch (IllegalArgumentException | ClassCastException e) {
                continue;
            }
            boolean create = true;
            try {
                create = (Boolean) rawListEntry.get("create");
            } catch (ClassCastException e) {
                // Do nothing, assume true
            }
            String[] blacklist = new String[0];
            if (rawListEntry.containsKey("blacklist")) {
                try {
                    blacklist = ((List<String>) rawListEntry.get("blacklist")).toArray(new String[0]);
                } catch (ClassCastException | ArrayStoreException ignored) {
                }
            }
            list.add(new BackupListEntry(location, formatter, create, blacklist));
        }
        return new BackupList(list.toArray(new BackupListEntry[0]));
    }
}
