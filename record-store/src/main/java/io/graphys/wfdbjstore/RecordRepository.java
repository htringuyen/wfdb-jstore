package io.graphys.wfdbjstore;

import io.graphys.wfdbjstore.exception.RecordConstructFailedException;
import io.graphys.wfdbjstore.header.HeaderReader;
import io.graphys.wfdbjstore.header.ReadHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;
import java.util.stream.IntStream;


public class RecordRepository {
    private static final Logger logger = LogManager.getLogger(RecordRepository.class);
    private final HeaderReader headerReader;
    private final StorageContext storageContext;
    private final CacheContext cacheContext;

    public RecordRepository(HeaderReader headerReader) {
        this.headerReader = headerReader;
        this.storageContext = StorageContext.get();
        this.cacheContext = CacheContext.get();
    }

    public Record findBy(PathInfo pathInfo) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            storageContext.awaitPrepareHeader(pathInfo.getRecordName(), pathInfo);
            if (headerReader.isMultiSegmentRecord(pathInfo.getRecordPath())) {
                var segmentInfo = headerReader.readSegmentInfo(pathInfo.getAbsoluteDir() + pathInfo.getRecordName());
                var subTasks = new ArrayList<StructuredTaskScope.Subtask<SegmentedRecord>>(segmentInfo.length);
                for (var sei: segmentInfo) {
                    var subTask = scope.fork(() -> {
                        storageContext.awaitPrepareHeader(sei.getRecordName(), pathInfo);
                        var signalInfo = headerReader.readSignalInfo(pathInfo.getAbsoluteDir() + sei.getRecordName());
                        return SegmentedRecord
                                .segmentBuilder()
                                .name(sei.getRecordName())
                                .signalInfo(signalInfo)
                                .build();
                    });
                    subTasks.add(subTask);
                }
                scope.join();
                scope.throwIfFailed();
                return MultiSegmentRecord
                        .multiSegmentBuilder()
                        .pathInfo(pathInfo)
                        .segmentInfo(segmentInfo)
                        .signalInfo(subTasks.removeFirst().get().getSignalInfo())
                        .segments(subTasks.stream().map(Subtask::get).toArray(SegmentedRecord[]::new))
                        .build();
            } else {
                return OrdinaryRecord
                        .builder()
                        .pathInfo(pathInfo)
                        .signalInfo(headerReader.readSignalInfo(pathInfo.getRecordPath()))
                        .build();
            }
        } catch (IOException | ExecutionException | InterruptedException e) {
            throw new RecordConstructFailedException("Construct record failed.", e);
        }
    }

    public Record findBy2(PathInfo pathInfo) {
        try {
            storageContext.awaitPrepareHeader(pathInfo.getRecordName(), pathInfo);
            var segmentInfo = headerReader.readSegmentInfo(pathInfo.getRecordPath());
            if (segmentInfo.length <= 0) {
                var readMsHeader = headerReader.readFullHeader(pathInfo.getRecordPath());
                return readMsHeader.createOrdinaryRecord(pathInfo);
            } else {
                var subTasks = new ArrayList<Subtask<ReadHeader>>();
                var recordNames = Arrays
                        .stream(segmentInfo)
                        .map(SegmentInfo::getRecordName)
                        .toList();
                try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                    for (var recordName: recordNames) {
                        var subTask = scope.fork(() -> {
                            storageContext.awaitPrepareHeader(recordName, pathInfo);
                            return headerReader.readFullHeader(pathInfo.getAbsoluteDir() + recordName);
                        });
                        subTasks.add(subTask);
                    }
                    scope.join();
                    scope.throwIfFailed();
                    var msHeader = subTasks.getFirst().get();
                    var segments = IntStream
                            .range(1, recordNames.size())
                            .mapToObj(i -> {
                                return subTasks.get(i).get().createSegmentedRecord(pathInfo, recordNames.get(i));
                            })
                            .toArray(SegmentedRecord[]::new);
                    return msHeader.createMultiSegmentRecord(pathInfo, segmentInfo, segments);
                } catch (InterruptedException | ExecutionException e) {
                    throw new RecordConstructFailedException("Construct record failed.", e);
                }
            }
        } catch (IOException e) {
            throw new RecordConstructFailedException("Construct record failed.", e);
        }
    }
}































