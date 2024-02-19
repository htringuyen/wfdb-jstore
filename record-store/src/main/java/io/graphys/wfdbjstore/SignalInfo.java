package io.graphys.wfdbjstore;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import wfdb.WFDB_Siginfo;

@Builder
@Setter
public class SignalInfo {
    private String fileName;

    private String description;

    private String units;

    private double gain;

    private int initValue;

    private long group;

    private int formatCode;

    private int spf;

    private int blockSize;

    private int adcResolution;

    private int adcZero;

    private int baseline;

    private long numOfSamples;

    public static SignalInfo from(WFDB_Siginfo si) {
        return SignalInfo.builder()
                .fileName(si.getFname())
                .description(si.getDesc())
                .units(si.getUnits() == null ? "mV" : si.getUnits())
                .gain(si.getGain())
                .initValue(si.getInitval())
                .group(si.getGroup())
                .formatCode(si.getFmt())
                .spf(si.getSpf())
                .blockSize(si.getBsize())
                .adcResolution(si.getAdcres())
                .adcZero(si.getAdczero())
                .baseline(si.getBaseline())
                .numOfSamples(si.getNsamp())
                .build();
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();
        builder
                .append("Description: ").append(description).append("\n")
                .append("File name: ").append(fileName).append("\n")
                .append("Units: ").append(units).append("\n")
                .append("Gain: ").append(gain).append("\n")
                .append("Init value: ").append(initValue).append("\n")
                .append("Group: ").append(formatCode).append("\n")
                .append("Spf: ").append(spf).append("\n")
                .append("Block size: ").append(blockSize).append("\n")
                .append("Adc resolution: ").append(adcResolution).append("\n")
                .append("Adc zero: ").append(adcZero).append("\n")
                .append("Baseline: ").append(baseline).append("\n")
                .append("Number of samples: ").append(numOfSamples);

        return builder.toString();
    }
}