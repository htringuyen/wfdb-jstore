package io.graphys.codectest.wfdb;

import wfdb.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WfdbToCsvConverter {
    private static final String WFDB_ROOT = "file:///home/nhtri/physionet.org/files/mimic4wdb/0.1.0";
    public static void convertWfdbToCsv(String record, String outName, int nSamp) {
        try (var printWriter = new PrintWriter(outName)) {
            wfdb.setwfdb(WFDB_ROOT);
            var nSigs = wfdb.isigopen(record, null, 0);
            var siArray = new WFDB_SiginfoArray(nSigs);
            var samples = new WFDB_SampleArray(nSigs);
            wfdb.isigopen(record, siArray.cast(), nSigs);
            IntStream
                    .range(0, nSamp)
                    .mapToObj(i -> {
                        wfdb.getvec(samples.cast());
                        return IntStream
                                .range(0, nSigs)
                                .map(samples::getitem)
                                .mapToObj(String::valueOf)
                                .collect(Collectors.joining(","));
                    })
                    //.forEach((s) -> {});
                    .forEach(printWriter::println);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            wfdb.wfdbquit();
        }
    }
}