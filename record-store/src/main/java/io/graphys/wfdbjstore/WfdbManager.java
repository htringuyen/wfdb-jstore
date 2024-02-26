package io.graphys.wfdbjstore;

import io.graphys.wfdbjstore.exception.DatabaseRegisterFailedException;
import io.graphys.wfdbjstore.header.HeaderReader;
import io.graphys.wfdbjstore.header.NativeHeaderReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WfdbManager {
    private static final Logger logger = LogManager.getLogger(WfdbManager.class);
    private final List<WfdbStore> wfdbStores = new LinkedList<>();
    private final Map<String, String> properties = new HashMap<>();
    private final HeaderReader headerReader = new NativeHeaderReader();
    private final RecordRepository recordRepo = new RecordRepository(headerReader);
    private static final WfdbManager singleton;

    static {
        singleton = new WfdbManager();
        singleton.loadProperties();
        singleton.loadDatabases();
    }

    public static WfdbManager get() {
        return singleton;
    }

    private WfdbManager() {

    }

    public int getNumDatabases() {
        return wfdbStores.size();
    }

    public WfdbStore getWfdbStore(String name, String version) {
        return wfdbStores
                .stream()
                .filter(s -> s.getDbInfo().name().equals(name) && s.getDbInfo().version().equals(version))
                .findFirst()
                .orElse(null);
    }

    public RecordRepository getRecordRepository() {
        return recordRepo;
    }

    public WfdbStore registerDatabase(String name, String version) {
        var registered = wfdbStores
                .stream()
                .map(WfdbStore::getDbInfo)
                .anyMatch(db -> db.name().equals(name) && db.version().equals(version));
        if (registered) {
            throw new DatabaseRegisterFailedException(
                    String.format("Database [name=%s, version=%s] has already registered.", name, version));
        }
        var dbInfo = newDatabaseInfo(name, version);
        CacheContext.get().register(dbInfo);
        return new WfdbStoreImpl(newDatabaseInfo(name, version));
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
                    .map(String::strip)
                    .filter(s -> !s.isBlank() && !s.startsWith("//"))
                    .map(pattern::matcher)
                    .filter(Matcher::matches)
                    .forEach(m -> wfdbStores.add(registerDatabase(m.group(1), m.group(2))));
        }
        catch (Exception e) {
            logger.info("stacktrace for debugging", e);
            throw new DatabaseRegisterFailedException("Error when loading databases...", e);
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
                    .map(String::strip)
                    .filter(s -> !s.isBlank() && !s.startsWith("//"))
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
                .localRoot(URI.create(properties.get("uri.localroot")))
                .remoteRoot(URI.create(properties.get("uri.remoteroot")))
                .build();
    }

    public List<String> getNativeWfdbPaths() {
        var result = new LinkedList<String>();
        result.add(properties.get("uri.localroot"));
        result.add(properties.get("uri.remoteroot"));
        return result;
    }

}


























