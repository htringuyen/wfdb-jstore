package io.graphys.wfdbjstore.io;

import io.graphys.wfdbjstore.SegmentInfo;
import io.graphys.wfdbjstore.SignalInfo;

public interface HeaderReader {
    boolean isMultiSegmentRecord();

    SegmentInfo[] getSegmentInfo();

    SignalInfo[] getSignalInfo();
}
