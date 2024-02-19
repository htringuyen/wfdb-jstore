package io.graphys.wfdbjstore;

public class WfdbStoreImpl implements WfdbStore {
    private DatabaseInfo dbInfo;
    private RecordInfoRetriever recInfoRetriever;
    private Skeleton skeleton;

    WfdbStoreImpl(DatabaseInfo dbInfo, CacheManager cacheManager) {
        this.dbInfo = dbInfo;
        cacheManager.register(dbInfo);
        this.skeleton = Skeleton.loadFor(dbInfo, false);
        this.recInfoRetriever = new BasicRecordInfoRetriever(dbInfo, cacheManager.recordInfoPoolOf(dbInfo));
    }

    @Override
    public DatabaseInfo getDbInfo() {
        return dbInfo;
    }

    @Override
    public RecordInfoRetriever getRecordInfoRetriever() {
        return recInfoRetriever;
    }

    @Override
    public Skeleton getSkeleton() {
        return skeleton;
    }

}
