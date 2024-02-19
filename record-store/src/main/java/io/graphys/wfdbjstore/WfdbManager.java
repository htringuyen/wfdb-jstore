package io.graphys.wfdbjstore;

import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WfdbManager {
    private final List<WfdbStore> wfdbStores = new LinkedList<>();
    private final CacheManager cacheManager = new CacheManager();
    private final Map<String, String> properties = new HashMap<>();
    private boolean initiated = false;
    private static final WfdbManager singleton = new WfdbManager();
    public static WfdbManager get() {
        if (!singleton.initiated) {
            singleton.loadProperties();
            singleton.loadDatabases();
            singleton.initiated = true;
        }
        return singleton;
    }

    private WfdbManager() {}

    public WfdbStore getWfdbStore(String name, String version) {
        return wfdbStores.stream()
                .filter(s -> s.getDbInfo().name().equals(name)
                        && s.getDbInfo().version().equals(version))
                .findFirst()
                .orElse(null);
    }

    public WfdbStore registerDatabase(String dbName, String version) {
        var dbInfo = newDatabaseInfo(dbName, version);
        var wfdbStore = new WfdbStoreImpl(dbInfo, cacheManager);
        wfdbStores.add(wfdbStore);
        return wfdbStore;
    }

    public Map<String,String> getProperties() {
        return properties;
    }

    private void loadDatabases() {
        try (
                var in = new Scanner(
                        Objects.requireNonNull(
                                this.getClass().getClassLoader().getResourceAsStream("database.list"))
                )
        ) {
            var pattern = Pattern.compile("^\\s*database:\\s*name\\s*=\\s*([\\w_.-]+)\\s*,\\s*version\\s*=\\s*([\\w_.-]+)\\s*$");
            in.useDelimiter("\n+");
            in.tokens()
                    .filter(s -> !s.isBlank())
                    .map(String::strip)
                    .map(pattern::matcher)
                    .filter(Matcher::matches)
                    .forEach(m -> {
                        /*var dbInfo = DatabaseInfo.builder()
                                .name(m.group(1))
                                .version(m.group(2))
                                .localHome(URI.create(properties.get("uri.localhome")))
                                .remoteHome(URI.create(properties.get("uri.remotehome")))
                                .build();*/
                        var dbInfo = newDatabaseInfo(m.group(1), m.group(2));
                        var store = new WfdbStoreImpl(dbInfo, cacheManager);
                        wfdbStores.add(store);
                    });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProperties() {
        try (var in = new Scanner(
                Objects.requireNonNull(
                        this.getClass().getClassLoader().getResourceAsStream("application.properties"))
                )
        ) {
            in.useDelimiter("\n+");
            in.tokens()
                    .filter(s -> !s.isBlank())
                    .forEach(s -> {
                        var startInd = s.indexOf('=');
                        properties.put(
                                s.substring(0, startInd).strip(),
                                s.substring(startInd + 1).strip()
                        );
                    });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DatabaseInfo newDatabaseInfo(String name, String version) {
        return DatabaseInfo.builder()
                .name(name)
                .version(version)
                .localHome(URI.create(properties.get("uri.localhome")))
                .remoteHome(URI.create(properties.get("uri.remotehome")))
                .build();
    }

}


























