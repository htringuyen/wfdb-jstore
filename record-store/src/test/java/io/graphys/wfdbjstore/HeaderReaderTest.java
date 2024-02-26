package io.graphys.wfdbjstore;

import io.graphys.wfdbjstore.header.HeaderReader;
import io.graphys.wfdbjstore.header.NativeHeaderReader;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HeaderReaderTest extends BaseTest {
    private final HeaderReader headerReader = new NativeHeaderReader();
    private final WfdbStore wfdbStore = wfdbManager.getWfdbStore("mimic4wdb", "0.1.0");
    private final StorageContext storageContext = StorageContext.get();

    @Test
    void testReadSegmentInfo() {
        var pathInfo = wfdbStore.findPathInfoOf("81739927");
        var segmentInfo = headerReader.readSegmentInfo(pathInfo.getRecordPath());
        Arrays.stream(segmentInfo).forEach(logger::info);
    }

    @Test
    void testReadMultiSegment_concurrent() {
        var pathInfo = wfdbStore.findPathInfoOf("81739927");
        var segmentInfo = headerReader.readSegmentInfo(pathInfo.getRecordPath());

        var subTasks = new LinkedList<Subtask<SegmentedRecord>>();
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (var sei: segmentInfo) {
                var subTask = scope.fork(() -> {
                    storageContext.awaitPrepareHeader(sei.getRecordName(), pathInfo);
                    var signalInfo = headerReader.readSignalInfo(pathInfo.getAbsoluteDir() + sei.getRecordName());
                    return SegmentedRecord
                            .segmentBuilder()
                            .name(sei.getRecordName())
                            .pathInfo(pathInfo)
                            .signalInfo(signalInfo)
                            .build();
                });
                subTasks.add(subTask);
            }

            scope.join();
            scope.throwIfFailed();
        } catch (InterruptedException | ExecutionException e) {
            logger.info("test error", e);
        }

        Record layoutSegment = null;
        List<Record> records = new LinkedList<>();
        for (var subTask: subTasks) {
            var record = subTask.get();
            if (record.getName().equals(segmentInfo[0].getRecordName())) {
                layoutSegment = record;
            } else {
                records.add(record);
            }
        }

        assertNotNull(layoutSegment);
        assertEquals(20, records.size());
        logger.info(layoutSegment);
        records.forEach(logger::info);
    }

    @Test
    void testReadMultiSegment_concurrent_multipleTimes() {
        var pathInfo = wfdbStore.findPathInfoOf("81739927");

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (int ignoreMe = 0; ignoreMe < 1_000; ignoreMe++) {
                scope.fork(() -> {
                    /*var segmentNames = IntStream
                            .range(0, 21)
                            .mapToObj(i -> String.format("%s_%04d", pathInfo.getRecordName(), i))
                            .toList();*/

                    var segmentNames =
                            Arrays
                                    .stream(headerReader.readSegmentInfo(pathInfo.getAbsoluteDir() + pathInfo.getRecordName()))
                                    .map(SegmentInfo::getRecordName)
                                    .toList();

                    var miniSubTasks = new LinkedList<Subtask<SegmentedRecord>>();
                    try (var miniScope = new StructuredTaskScope.ShutdownOnFailure()) {
                        /*var miniSubTask = miniScope.fork(() -> {
                            for (var name: segmentNames) {
                                var signalInfo = headerReader.readSignalInfo(pathInfo.getAbsoluteDir() + name);
                            }
                            return SegmentedRecord
                                    .segmentBuilder()
                                    .pathInfo(pathInfo)
                                    .signalInfo();
                        });*/
                        for (var name: segmentNames) {
                            miniScope.fork(() -> {
                                var signalInfo = headerReader.readSignalInfo(pathInfo.getAbsoluteDir() + name);
                                return signalInfo;
                            });
                        }
                        miniScope.join();
                        miniScope.throwIfFailed();
                    }
                    return null;
                });
                scope.join();
                scope.throwIfFailed();
            }
        } catch (Exception e) {
            logger.info("error", e);
        }
    }
}



























