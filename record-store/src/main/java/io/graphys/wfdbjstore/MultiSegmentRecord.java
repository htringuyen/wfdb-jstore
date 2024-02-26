package io.graphys.wfdbjstore;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class MultiSegmentRecord extends OrdinaryRecord {
    private final SegmentInfo[] segmentInfo;
    private SegmentedRecord[] segments;

    @Builder(builderMethodName = "multiSegmentBuilder")
    public MultiSegmentRecord(PathInfo pathInfo, SignalInfo[] signalInfo, String[] textInfo, SegmentInfo[] segmentInfo,
                              LocalDateTime baseTime, double sampFreq, SegmentedRecord[] segments) {
        super(pathInfo, signalInfo, textInfo, baseTime, sampFreq);
        this.segmentInfo = segmentInfo;
        this.segments = segments;
    }
}
