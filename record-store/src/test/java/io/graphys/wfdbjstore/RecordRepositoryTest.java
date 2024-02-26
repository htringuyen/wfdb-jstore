package io.graphys.wfdbjstore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;

import static org.junit.jupiter.api.Assertions.*;

public class RecordRepositoryTest extends BaseTest {
    private RecordRepository recordRepo;
    private WfdbStore wfdbStore;
    private CacheContext cacheContext;

    @BeforeEach
    void initObjects() {
        recordRepo = wfdbManager.getRecordRepository();
        wfdbStore = wfdbManager.getWfdbStore("mimic4wdb", "0.1.0");
        cacheContext = CacheContext.get();
    }

    @Test
    void  testFindOrdinaryRecord_concurrent() throws IOException {
        wfdbStore = wfdbManager.getWfdbStore("mimic-iv-ecg", "1.0");
        var pathInfo = wfdbStore.findPathInfoOf("40689238");
        var file = new File(pathInfo.formLocalURIWith("40689238.hea"));

        var preExisted = file.exists();

        var subTasks = new LinkedList<StructuredTaskScope.Subtask<Record>>();
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (int i = 0; i < 10_000; i++) {
                var subTask = scope.fork(() -> recordRepo.findBy(pathInfo));
                subTasks.add(subTask);
            }
            scope.join();
            scope.throwIfFailed();
        } catch (InterruptedException | ExecutionException e) {
            logger.info("error when test", e);
            throw new RuntimeException("error when test");
        }

        subTasks
                .forEach(task -> assertEquals(12, task.get().getSignalInfo().length));

        assertTrue(file.exists());

        if (!preExisted) {
            assertTrue(file.delete());
        }
    }

    @Test
    void testFindMultiSegmentRecord_concurrent() {
        var record = "81739927";
        var pathInfo = wfdbStore.findPathInfoOf(record);

        var subTasks = new LinkedList<StructuredTaskScope.Subtask<Record>>();
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (int i = 0; i < 1_000; i++) {
                var subTask = scope.fork(() -> {
                    var msRecord = recordRepo.findBy(pathInfo);
                    return msRecord;
                });
                subTasks.add(subTask);
            }
            scope.join();
            scope.throwIfFailed();
        } catch (InterruptedException | ExecutionException e) {
            logger.info("stacktrace for debug", e);
        }

        subTasks.forEach(st -> {
            assertEquals(7, st.get().getSignalInfo().length);
        });
    }
}






























