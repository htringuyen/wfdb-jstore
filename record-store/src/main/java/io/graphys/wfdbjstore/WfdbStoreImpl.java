package io.graphys.wfdbjstore;

public class WfdbStoreImpl implements WfdbStore {
    private final Skeleton skeleton;
    private final CacheContext cacheContext;
    private final DatabaseInfo dbInfo;

    WfdbStoreImpl(DatabaseInfo dbInfo) {
        this.skeleton = new SkeletonImpl(dbInfo);
        this.cacheContext = CacheContext.get();
        this.dbInfo = dbInfo;
    }

    @Override
    public Skeleton getSkeleton() {
        return skeleton;
    }

    @Override
    public PathInfo findPathInfoOf(String recordName) {
        var pathInfo = cacheContext.retrievePathInfo(recordName, dbInfo);
        if (pathInfo != null) return pathInfo;
        pathInfo = skeleton.findPathInfoOf(recordName);
        cacheContext.cachePathInfo(pathInfo, recordName, dbInfo);
        return pathInfo;
    }

    @Override
    public PathInfo[] findAllPathInfo() {
        return skeleton.findAllPathInfo();
    }

    @Override
    public String[] findPathSegments(int ordinal) {
        return skeleton.findPathSegments(ordinal);
    }

    @Override
    public boolean isBuilt() {
        return skeleton.isBuilt();
    }

    @Override
    public void build() {
        skeleton.build();
    }

    @Override
    public DatabaseInfo getDbInfo() {
        return dbInfo;
    }
}



























