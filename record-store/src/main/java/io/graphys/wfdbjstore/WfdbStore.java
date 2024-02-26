package io.graphys.wfdbjstore;

public interface WfdbStore {
    Skeleton getSkeleton();

    PathInfo findPathInfoOf(String recordName);

    PathInfo[] findAllPathInfo();

    String[] findPathSegments(int ordinal);

    boolean isBuilt();

    void build();

    DatabaseInfo getDbInfo();
}
