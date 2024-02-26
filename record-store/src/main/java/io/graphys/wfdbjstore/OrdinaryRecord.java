package io.graphys.wfdbjstore;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

public class OrdinaryRecord implements Record {
    protected final PathInfo pathInfo;
    protected final SignalInfo[] signalInfo;
    protected final double sampFreq;
    protected final LocalDateTime baseTime;
    protected final String[] textInfo;


    @Builder
    public OrdinaryRecord(PathInfo pathInfo, SignalInfo[] signalInfo, String[] textInfo, LocalDateTime baseTime, double sampFreq) {
        this.pathInfo = pathInfo;
        this.signalInfo = signalInfo;
        this.textInfo = textInfo;
        this.baseTime = baseTime;
        this.sampFreq = sampFreq;
    }

    @Override
    public LocalDateTime getBaseTime() {
        return baseTime;
    }

    @Override
    public double getSampFreq() {
        return sampFreq;
    }


    @Override
    public SignalInfo[] getSignalInfo() {
        return signalInfo;
    }

    @Override
    public String getName() {
        return pathInfo.getRecordName();
    }

    @Override
    public String toString() {
        return String.format(
                "Record name: %s%nSignal Info:%n%s",
                getName(),
                Arrays
                        .stream(signalInfo)
                        .map(Object::toString)
                        .collect(Collectors.joining("\n"))
                );
    }
}




















