package io.graphys.wfdbjstore;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static io.graphys.wfdbjstore.Skeleton.RECORD_PATHS_FN;

/**
 * A basic implementation of RecordInfoRetriever interface.
 */
public class BasicRecordInfoRetriever implements RecordInfoRetriever {
    private DatabaseInfo dbInfo;
    private CachePool<String, RecordInfo> cachePool;

    /**
     * Instantiate the retriever for given database
     * @param dbInfo The DatabaseInfo of given database
     * @param cachePool The CachePool used for caching operations of the retriever
     */
    BasicRecordInfoRetriever(DatabaseInfo dbInfo, CachePool<String, RecordInfo> cachePool) {
        this.dbInfo = dbInfo;
        this.cachePool = cachePool;
    }

    @Override
    public List<RecordInfo> getAll() {
        try (var in = new Scanner(Path.of(dbInfo.localUri().resolve(RECORD_PATHS_FN)))) {
            return in
                    .tokens()
                    .parallel()
                    .map(this::parseRecordInfo)
                    .toList();
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error when open file: " + dbInfo.localUri().resolve(RECORD_PATHS_FN));
        }
    }

    @Override
    public RecordInfo getByRecordName(String recordName) {
        if (recordName == null) {
            return null;
        }

        var result = cachePool.get(recordName);
        if (result != null) {
            return result;
        }

        try (var in = new Scanner(Path.of(dbInfo.localUri().resolve(RECORD_PATHS_FN)))) {
            var recordPath = in.tokens()
                    .parallel()
                    .filter(s -> s.startsWith(recordName + ","))
                    .map(this::parseRecordInfo)
                    .findFirst()
                    .orElse(null);

            if (recordPath != null) {
                cachePool.put(recordName, recordPath);
            }

            return recordPath;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<String> getPathSegments(int level) {
        if (level < 1) {
            return new LinkedList<>();
        }

        try (var in = new Scanner(Path.of(dbInfo.localUri().resolve(RECORD_PATHS_FN)))) {
            return in.tokens()
                    .map(s -> {
                        var columns = s.split(",");
                        if (level < columns.length) {
                            return columns[level];
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error when open file " + dbInfo.localUri().resolve(RECORD_PATHS_FN));
        }
    }

    private RecordInfo parseRecordInfo(String str) {
        var columns = str.split(",");
        if (columns[0].isBlank()
                || columns.length < 2) {
            throw new RuntimeException("Error when reading file " + RECORD_PATHS_FN);
        }
        return RecordInfo
                .builder()
                .dbInfo(dbInfo)
                .name(columns[0])
                .pathSegments(Arrays.copyOfRange(columns, 1, columns.length))
                .build();
    }
}
