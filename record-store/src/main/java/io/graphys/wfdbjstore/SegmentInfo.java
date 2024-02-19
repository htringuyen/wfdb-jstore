package io.graphys.wfdbjstore;

import lombok.Builder;
import lombok.Getter;
import wfdb.WFDB_Seginfo;

@Builder
@Getter
public class SegmentInfo {
    private String recordName;
    private int numSamples;
    private int numPrevSamples;

    public static SegmentInfo from(WFDB_Seginfo sei) {
        return SegmentInfo
                .builder()
                .recordName(sei.getRecname())
                .numSamples(sei.getNsamp())
                .numPrevSamples(sei.getSamp0())
                .build();
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();
        builder
                .append("Record name: ").append(recordName).append("\n")
                .append("Num samples: ").append(numSamples).append("\n")
                .append("Num prev samples: ").append(numPrevSamples).append("\n");
        return builder.toString();
    }
}
