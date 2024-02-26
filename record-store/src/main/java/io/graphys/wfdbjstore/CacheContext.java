package io.graphys.wfdbjstore;

import java.util.HashMap;
import java.util.Map;

/**
 * Managing cache pools of retrieved objects, currently support caching of RecordInfo and Record.
 */
public class CacheContext {
    public static final int DEFAULT_PATH_INFO_CACHE_LIMIT_DB = 1000;
    public static final int DEFAULT_PATH_INFO_CACHE_LIMIT_ALL = 5000;
    public static final int DEFAULT_RECORD_CACHE_LIMIT_DB = 1000;
    public static final int DEFAULT_RECORD_CACHE_LIMIT_ALL = 5000;
    private final Map<DatabaseInfo, ObjectCache<String, PathInfo>> pathInfoCaches = new HashMap<>();
    private final Map<DatabaseInfo, ObjectCache<String, Record>> recordCaches = new HashMap<>();
    private static final CacheContext singleton;

    static {
        singleton = new CacheContext();
    }

    public static CacheContext get() {
        return singleton;
    }

    private CacheContext() {
    }

    /**
     * Register new database to the manager
     * @param dbInfo info of registered database
     */
    public void register(DatabaseInfo dbInfo) {
        if (pathInfoCaches.containsKey(dbInfo) || recordCaches.containsKey(dbInfo)) return;
        var pathInfoCache = new ObjectCacheImpl<String, PathInfo>(DEFAULT_PATH_INFO_CACHE_LIMIT_DB);
        var recordCache = new ObjectCacheImpl<String, Record>(DEFAULT_RECORD_CACHE_LIMIT_DB);
        pathInfoCaches.put(dbInfo, pathInfoCache);
        recordCaches.put(dbInfo, recordCache);
    }

    /**
     * Return CachePool of RecordInfo for the given registered database.
     * @param dbInfo The given database info
     * @return CachePool of RecordInfo for the given registered database
     */
    /*private <T> ObjectCache<String, T> getCache(Class<T> clazz) {
        var dbInfo = DatabaseContext.getDB_INFO();
        if (!pathInfoCaches.containsKey(dbInfo)) {
            return null;
        }
        return pathInfoCaches.get(dbInfo);
    }*/

    public PathInfo retrievePathInfo(String recordName, DatabaseInfo dbInfo) {
        return pathInfoCaches.get(dbInfo).get(recordName);
    }

    public void cachePathInfo(PathInfo pathInfo, String recordName, DatabaseInfo dbInfo) {
        pathInfoCaches.get(dbInfo).put(recordName, pathInfo);
    }

    public Record retrieveRecord(String recordName, DatabaseInfo dbInfo) {
        return recordCaches.get(dbInfo).get(recordName);
    }

    public void cacheRecord(Record record, String recordName, DatabaseInfo dbInfo) {
        recordCaches.get(dbInfo).put(recordName, record);
    }
}










































