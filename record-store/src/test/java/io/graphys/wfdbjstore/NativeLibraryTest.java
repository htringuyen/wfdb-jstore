package io.graphys.wfdbjstore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import wfdb.*;

import java.util.stream.IntStream;

public class NativeLibraryTest extends BaseTest {
    private RecordInfoRetriever recInfoRev;

    @BeforeEach
    void doBeforeTest() {
        recInfoRev = wfdbManager
                .getWfdbStore("mimic4wdb", "0.1.0")
                .getRecordInfoRetriever();
    }

    @Test
    void testGetSegmentInfo() {
        var recInfo = recInfoRev.getByRecordName("81739927");
        wfdb.isigopen(recInfo.getEntryPoint(), null, 0);
        var nSeg = wfdb.getseginfocount();
        var seginfoArray = new WFDB_SeginfoArray(wfdb.getseginfoptr(), true);

        IntStream.range(0, nSeg)
                .mapToObj(seginfoArray::getitem)
                .forEach(sei -> {
                    logger.info("recname: {}", sei.getRecname());
                    logger.info("nsamp: {}", sei.getNsamp());
                    logger.info("samp0: {}", sei.getSamp0());
                    logger.info("----------------");
                });
    }

    @Test
    void testGetSignalInfo() {
        var recInfo = recInfoRev.getByRecordName("81739927");
        var nSig = wfdb.isigopen(recInfo.getEntryPoint(), null, 0);
        var siArray = new WFDB_SiginfoArray(nSig);
        wfdb.isigopen(recInfo.getEntryPoint(), siArray.cast(), -nSig);
        IntStream.range(0, nSig)
                .mapToObj(siArray::getitem)
                .map(SignalInfo::from)
                .forEach(logger::info);
    }

    @Test
    void testGetSegmentInfo_mimicIvEcg() {
        var recInfoRev = wfdbManager
                .registerDatabase("mimic-iv-ecg", "1.0")
                .getRecordInfoRetriever();

        var recInfo = recInfoRev.getByRecordName("40689238");
        logger.info("Entry point: {}", recInfo.getEntryPoint());
        wfdb.isigopen(recInfo.getEntryPoint(), null, 0);
        var nSeg = wfdb.getseginfocount();
        var seginfoArray = new WFDB_SeginfoArray(wfdb.getseginfoptr(), true);

        logger.info("nseg: {}", nSeg);
        logger.info("seginfoptr: {}", wfdb.getseginfoptr());

        IntStream.range(0, nSeg)
                .mapToObj(seginfoArray::getitem)
                .forEach(sei -> {
                    logger.info("recname: {}", sei.getRecname());
                    logger.info("nsamp: {}", sei.getNsamp());
                    logger.info("samp0: {}", sei.getSamp0());
                    logger.info("----------------");
                });
    }
}





















