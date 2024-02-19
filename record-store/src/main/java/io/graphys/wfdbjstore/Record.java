package io.graphys.wfdbjstore;


public interface Record {
    public DatabaseInfo getDbInfo();

    public String getId();

    public RecordInfo getRecordInfo();

    public SignalInfo[] getSignalInfo();
}
