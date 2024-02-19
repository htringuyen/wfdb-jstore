package io.graphys.wfdbjstore;

import lombok.Builder;

@Builder
public class OrdinaryRecord implements Record {
    private DatabaseInfo dbInfo;
    private SignalInfo[] signalInfo;
    private RecordInfo recordInfo;

    @Override
    public String getId() {
        return dbInfo.name() + "-" + dbInfo.version() + "-" + recordInfo.getName();
    }

    @Override
    public DatabaseInfo getDbInfo() {
        return dbInfo;
    }

    @Override
    public RecordInfo getRecordInfo() {
        return recordInfo;
    }

    @Override
    public SignalInfo[] getSignalInfo() {
        return signalInfo;
    }
}
