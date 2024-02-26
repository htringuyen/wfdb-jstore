package io.graphys.wfdbjstore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import wfdb.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.stream.IntStream;

public class NativeLibraryTest extends BaseTest {
    private WfdbStore wfdbStore;

    @BeforeEach
    void doBeforeTest() {
        wfdbStore = wfdbManager.getWfdbStore("mimic4wdb", "0.1.0");
    }

    @Test
    void testGetSegmentInfo() {
        var pathInfo = wfdbStore.findPathInfoOf("81739927");
        wfdb.isigopen(STR."\{pathInfo.getRecordPath()}.hea", null, 0);
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
        var pathInfo = wfdbStore.findPathInfoOf("81739927");
        var nSig = wfdb.isigopen(STR."\{pathInfo.getRecordPath()}.hea", null, 0);
        var siArray = new WFDB_SiginfoArray(nSig);
        wfdb.isigopen(STR."\{pathInfo.getRecordPath()}.hea", siArray.cast(), -nSig);
        IntStream.range(0, nSig)
                .mapToObj(siArray::getitem)
                .map(SignalInfo::from)
                .forEach(logger::info);
    }

    /*@Test
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
    }*/

    @Test
    void testGetSamplingFrequency() {
        var record = "81739927";
        var pathInfo = wfdbStore.findPathInfoOf(record);
        var sampFreq = wfdb.sampfreq(pathInfo.getRecordPath());
        logger.info("Record {} freq: {}", record, sampFreq);
    }

    @Test
    void testGetTime() {
        //wfdbStore = wfdbManager.getWfdbStore("mimic-iv-ecg", "1.0");
        var record = "81739927";
        var pathInfo = wfdbStore.findPathInfoOf(record);
        wfdb.isigopen(pathInfo.getRecordPath(), null, 0);
        var time = wfdb.mstimstr(0);
        logger.info(time);
    }

    @Test
    void testReadInfo() {
        var record = "81739927";
        var pathInfo = wfdbStore.findPathInfoOf(record);
        wfdb.isigopen(pathInfo.getRecordPath(), null, 0);
        var infoList = new LinkedList<String>();
        String info = null;
        while ((info = wfdb.getinfo(null)) != null) {
            infoList.add(info);
        }
        infoList.forEach(logger::info);
    }

    @Test
    void testGetTime_andConvert() {
        var record = "81739927";
        var pathInfo = wfdbStore.findPathInfoOf(record);
        wfdb.isigopen(pathInfo.getRecordPath(), null, 0);
        var time = wfdb.mstimstr(0);
    }

    @Test
    void testTimeConvert() {
        var record = "81739927";
        var pathInfo = wfdbStore.findPathInfoOf(record);
        wfdb.isigopen(pathInfo.getRecordPath(), null, 0);

        var timeStr = wfdb.mstimstr(0);
        timeStr = timeStr.substring(1, timeStr.length() - 1);

        timeStr = "01:01:01.342 01/01/0001";

        var formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS dd/MM/yyyy");
        try {
            var dateTime = LocalDateTime.parse(timeStr, formatter);
            logger.info(dateTime);
        } catch (Exception e) {
            logger.info("error", e);
        }
    }
}





















