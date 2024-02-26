package io.graphys.wfdbjstore;

import io.graphys.wfdbjstore.io.CacheInputCoordinator;
import io.graphys.wfdbjstore.io.CacheInputCoordinatorImpl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class StorageContext {
    private final Map<String, CacheInputCoordinator> coordinators = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();
    private static final StorageContext singleton;

    static {
        singleton = new StorageContext();
    }

    public static StorageContext get() {
        return singleton;
    }

    private StorageContext() {

    }

    public void awaitPrepareHeader(String header, PathInfo pathInfo) throws IOException {
        if (!header.endsWith(".hea")) header = header + ".hea";

        var coordinator = getCoordinator(header, pathInfo);
        coordinator.awaitPrepareCache(false);
    }

    private CacheInputCoordinator getCoordinator(String fileName, PathInfo pathInfo) throws IOException {
        var id = formIdFrom(fileName, pathInfo.getDbInfo());
        lock.lock();
        try {
            CacheInputCoordinator coordinator = null;
            if ((coordinator = coordinators.get(id)) == null) {
                coordinator = new CacheInputCoordinatorImpl(
                        pathInfo.formRemoteURIWith(fileName).toURL(),
                        new File(pathInfo.formLocalURIWith(fileName)),
                        null);
                coordinators.put(id, coordinator);
            }
            return coordinator;
        } finally {
            lock.unlock();
        }
    }

    private String formIdFrom(String fileName, DatabaseInfo dbInfo) {
        return dbInfo.name() + "-" + dbInfo.version() + "-" + fileName;
    }
}




























