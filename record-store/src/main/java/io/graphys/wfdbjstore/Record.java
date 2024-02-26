package io.graphys.wfdbjstore;

import java.time.LocalDateTime;
import java.util.List;

public interface Record {
    SignalInfo[] getSignalInfo();

    String getName();

    LocalDateTime getBaseTime();

    double getSampFreq();
}
