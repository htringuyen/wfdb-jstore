package io.graphys.wfdbjstore.header;

import io.graphys.wfdbjstore.Record;
import io.graphys.wfdbjstore.SegmentInfo;
import io.graphys.wfdbjstore.SignalInfo;

public interface HeaderReader {
    boolean isMultiSegmentRecord(String recordPath);

    SegmentInfo[] readSegmentInfo(String recordPath);

    SignalInfo[] readSignalInfo(String recordPath);

    ReadHeader readFullHeader(String recordPath);
}
