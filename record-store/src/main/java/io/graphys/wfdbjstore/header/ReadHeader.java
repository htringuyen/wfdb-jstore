package io.graphys.wfdbjstore.header;

import io.graphys.wfdbjstore.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Getter
@Builder
public class ReadHeader {
    public static final DateTimeFormatter BASE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS dd/MM/yyyy");

    private SignalInfo[] signalInfo;
    private SegmentInfo[] segmentInfo;
    private String baseTimeStr;
    private double sampFreq;
    private List<String> infoList;

    public boolean isMultiSegment() {
        return segmentInfo != null;
    }

    private LocalDateTime parseDataTime(String timeStr) {
        try {
            return LocalDateTime.parse(timeStr, BASE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public OrdinaryRecord createOrdinaryRecord(PathInfo pathInfo) {
        return OrdinaryRecord
                .builder()
                .signalInfo(signalInfo)
                .pathInfo(pathInfo)
                .baseTime(parseDataTime(baseTimeStr))
                .textInfo(infoList.toArray(String[]::new))
                .sampFreq(sampFreq)
                .build();
    }

    public MultiSegmentRecord createMultiSegmentRecord(PathInfo pathInfo, SegmentInfo[] segmentInfo, SegmentedRecord[] segments) {
        if (!isMultiSegment()) {
            throw new IllegalStateException("This is not a header of multi-segment record.");
        }
        return MultiSegmentRecord
                .multiSegmentBuilder()
                .pathInfo(pathInfo)
                .signalInfo(signalInfo)
                .baseTime(parseDataTime(baseTimeStr))
                .sampFreq(sampFreq)
                .textInfo(infoList.toArray(String[]::new))
                .segmentInfo(segmentInfo)
                .segments(segments)
                .build();
    }

    public SegmentedRecord createSegmentedRecord(PathInfo pathInfo, String name) {
        if (!isMultiSegment()) {
            throw new IllegalStateException("This is a multi segment record");
        }
        return SegmentedRecord
                .segmentBuilder()
                .pathInfo(pathInfo)
                .textInfo(infoList.toArray(String[]::new))
                .signalInfo(signalInfo)
                .sampFreq(sampFreq)
                .baseTime(parseDataTime(baseTimeStr))
                .name(name)
                .build();
    }
}

























