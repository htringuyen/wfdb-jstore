package io.graphys.wfdbjstore;

public interface WfdbStore {
    DatabaseInfo getDbInfo();

    RecordInfoRetriever getRecordInfoRetriever();

    Skeleton getSkeleton();
}
