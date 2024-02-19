package io.graphys.wfdbjstore;

import java.util.HashMap;
import java.util.Map;

/**
 * Managing cache pools of retrieved objects, currently support caching of RecordInfo and Record.
 */
public class CacheManager {
    public static final int DEFAULT_RECORD_INFO_CACHE_LIMIT_DB = 1000;
    public static final int DEFAULT_RECORD_INFO_CACHE_LIMIT_ALL = 5000;
    private final Map<DatabaseInfo, CachePool<String,RecordInfo>> recInfoPools;

    CacheManager() {
        recInfoPools = new HashMap<>();
    }

    /**
     * Register new database to the manager
     * @param dbInfo info of registered database
     */
    public void register(DatabaseInfo dbInfo) {
        if (recInfoPools.containsKey(dbInfo)) return;
        var recInfoPool = new HashCachePool<String, RecordInfo>(DEFAULT_RECORD_INFO_CACHE_LIMIT_DB);
        recInfoPools.put(dbInfo, recInfoPool);
    }

    /**
     * Return CachePool of RecordInfo for the given registered database.
     * @param dbInfo The given database info
     * @return CachePool of RecordInfo for the given registered database
     */
    public CachePool<String, RecordInfo> recordInfoPoolOf(DatabaseInfo dbInfo) {
        if (!recInfoPools.containsKey(dbInfo)) {
            return null;
        }
        return recInfoPools.get(dbInfo);
    }
}

























