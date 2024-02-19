package io.graphys.wfdbjstore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.StopWatch;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LocalStorageContextTest extends BaseTest {
    private RecordInfoRetriever recInfoRev;

    private WfdbStore store;

    @BeforeEach
    void doBeforeTest() {
        store = wfdbManager.getWfdbStore("mimic4wdb", "0.1.0");
        recInfoRev = store.getRecordInfoRetriever();
    }

    @Test
    void testRequireHeader() {
        var recInfo = recInfoRev.getByRecordName("81739927");

        var lsCtx = new LocalStorageContext(store.getDbInfo());
        var sw = new StopWatch();
        sw.start();
        lsCtx.requireHeader(recInfo.getName(), recInfo);
        sw.stop();
        logger.info("Downloading in: " + sw.getTotalTime(TimeUnit.MILLISECONDS) + " ms.");
    }

    @Test
    void testRequireHeader_multiThread() throws InterruptedException {
        var es = Executors.newVirtualThreadPerTaskExecutor();
        for (int i = 0; i < 1000; i++) {
            es.submit(this::testRequireHeader);
        }
        es.shutdown();
        es.awaitTermination(60, TimeUnit.SECONDS);
    }
}
