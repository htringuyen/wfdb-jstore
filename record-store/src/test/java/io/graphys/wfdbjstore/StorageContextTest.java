package io.graphys.wfdbjstore;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StorageContextTest extends BaseTest {
    private final StorageContext storageContext = StorageContext.get();
    private final WfdbStore wfdbStore = wfdbManager.getWfdbStore("mimic4wdb", "0.1.0");

    @Test
    void testAwaitPrepareHeader() throws IOException {
        var pathInfo = wfdbStore.findPathInfoOf("81739927");
        var file = new File(pathInfo.formLocalURIWith("81739927.hea"));
        file.delete();
        assertFalse(file.exists());
        storageContext.awaitPrepareHeader("81739927.hea", pathInfo);
        assertTrue(file.exists());
        assertTrue(file.delete());
    }

    @Test
    void testAwaitPrepareHeader_concurrency() {
        var pathInfo = wfdbStore.findPathInfoOf("81739927");
        var file = new File(pathInfo.formLocalURIWith("81739927.hea"));
        file.delete();
        assertFalse(file.exists());
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (int i = 0; i < 1000; i++) {
                scope.fork(() -> {
                    var start = Instant.now();
                    storageContext.awaitPrepareHeader("81739927.hea", pathInfo);
                    assertTrue(file.exists());
                    logger.info("Waiting time: {}", Duration.between(start, Instant.now()).toMillis());
                    return null;
                });
            }
            scope.join();
            scope.throwIfFailed();
        }
        catch (InterruptedException | ExecutionException e) {
            logger.info("stacktrace", e);
        }

        assertTrue(file.delete());
    }
}
