package io.graphys.wfdbjstore;

import lombok.Builder;

import java.time.LocalDateTime;

public class SegmentedRecord extends OrdinaryRecord {
    private final String name;

    @Builder(builderMethodName = "segmentBuilder")
    public SegmentedRecord(String name, PathInfo pathInfo, SignalInfo[] signalInfo,
                           String[] textInfo, LocalDateTime baseTime, double sampFreq) {
        super(pathInfo, signalInfo, textInfo, baseTime, sampFreq);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}
