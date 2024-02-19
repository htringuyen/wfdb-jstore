package io.graphys.wfdbjstore;

import io.graphys.wfdbjstore.util.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Provides methods related to storage structure of the wfdb store, internally constructing and ensuring the correctness of local file structure.
 */
public class Skeleton {
    public static final String PATH_SEGMENT_LIST_FN = "RECORDS";
    public static final String RECORD_PATHS_FN = "RECORD_PATHS.csv";
    public static final String SEGMENT_BOUNDARY_MARK = ">>";
    private DatabaseInfo dbInfo;

    private Skeleton(DatabaseInfo dbInfo) {
        this.dbInfo = dbInfo;
    }


    /**
     * Instantiates skeleton instance of specific database, also takes some actions to ensure that local
     * storage structure has already built at the time of instantiating.
     * @param dbInfo Info of target database of skeleton
     * @param forceRebuild If true, local file structure will be rebuilt anyway.
     *                     Otherwise, rebuild only if the current file structure is considered incorrect.
     * @return The skeleton object
     */
    static Skeleton loadFor(DatabaseInfo dbInfo, boolean forceRebuild) {
        var skeleton = new Skeleton(dbInfo);
        if (forceRebuild || !skeleton.storeExists()) {
            skeleton.buildSkeleton();
        }
        return skeleton;
    }

    private boolean storeExists() {
        var dbUri = dbInfo.localUri();
        return Files.isDirectory(Path.of(dbUri))
                && Files.exists(Path.of(dbUri.resolve(PATH_SEGMENT_LIST_FN)))
                && Files.exists(Path.of(dbUri.resolve(RECORD_PATHS_FN)));
    }

    private void buildSkeleton() {
        var recordPaths = Collections.synchronizedList(new LinkedList<String>());
        var pathAppenders = new ConcurrentHashMap<URI, List<String>>();
        pathAppenders.put(dbInfo.localUri(), Collections.synchronizedList(new LinkedList<>()));

        var dir = new File(dbInfo.localUri());
        if (! dir.isDirectory() && ! dir.mkdirs()) {
            throw new RuntimeException("Error when creating directory: " + dir);
        }
        buildSkeletonHelper(dbInfo.remoteUri(), dbInfo.localUri(), "",
                pathAppenders, 1, new HashMap<Integer, String>(), recordPaths);


        try (var out = new PrintWriter(
                new File(dbInfo.localUri().resolve(RECORD_PATHS_FN)))) {
            recordPaths.stream()
                    .map(segmentedPath -> {
                        var result = new StringBuilder();
                        var purePath = segmentedPath.replace(SEGMENT_BOUNDARY_MARK, "");
                        var recordName = extractRecordNameFrom(purePath);
                        segmentedPath = segmentedPath.substring(0, segmentedPath.lastIndexOf(recordName));
                        result.append(recordName).append(",");
                        result.append(
                                Arrays.stream(segmentedPath.split(SEGMENT_BOUNDARY_MARK))
                                        .filter(s -> !s.isBlank())
                                        .collect(Collectors.joining(","))
                        );
                        return result.toString();
                    })
                    .sorted()
                    .forEach(out::println);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "error when open file: " + dbInfo.remoteUri().resolve(RECORD_PATHS_FN));
        }



        pathAppenders.forEach((uri, list) -> {
            try (var out = new PrintWriter(new File(uri.resolve(PATH_SEGMENT_LIST_FN)))) {
                list.stream().sorted().forEach(out::println);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException("error when open file: " + uri);
            }
        });
    }

    private void buildSkeletonHelper(URI remoteUri, URI localUri, String segmentablePath, Map<URI, List<String>> pathAppenders,
                                        int level, Map<Integer, String> levelMap, List<String> recordPaths) {
        try (var in = new Scanner(remoteUri.resolve(PATH_SEGMENT_LIST_FN).toURL().openStream())) {
            in.tokens().parallel()
                    .filter(s -> !s.isBlank())
                    .forEach(str -> {
                        pathAppenders.get(localUri).add(str);

                        if (! Files.isDirectory(Path.of(localUri))) {
                            throw new RuntimeException("Directory should exist.");
                        }

                        var segmentListUri = remoteUri.resolve(str.endsWith("/") ? str : str + "/")
                                .resolve(PATH_SEGMENT_LIST_FN);
                        if (synchronizedCheckFileExists(segmentListUri, level, levelMap)) {
                            var appendedDir = new File(localUri.resolve(str));
                            if (! appendedDir.isDirectory() && ! appendedDir.mkdirs()) {
                                throw new RuntimeException("Error when creating directory.");
                            }

                            pathAppenders.put(localUri.resolve(str), new LinkedList<>());

                            buildSkeletonHelper(remoteUri.resolve(str), localUri.resolve(str),
                                    segmentablePath + SEGMENT_BOUNDARY_MARK + str,
                                    pathAppenders, level + 1, levelMap, recordPaths);
                        }
                        else {
                            recordPaths.add(segmentablePath + SEGMENT_BOUNDARY_MARK + str);
                            if (str.contains("/")) {
                                var appendedDir = new File(localUri.resolve(str.substring(0, str.lastIndexOf("/"))));
                                if (! appendedDir.isDirectory() && ! appendedDir.mkdirs()) {
                                    throw new RuntimeException("Error when creating directory.");
                                }
                            }
                        }
                    });
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error when reading record path.");
        }
    }

    private synchronized boolean synchronizedCheckFileExists(URI uri, int level, Map<Integer, String> levelMap) {
        if (! levelMap.containsKey(level)) {
            levelMap.put(level, "UNKNOWN");
        }

        var status = levelMap.get(level);

        if (status.equals("UNKNOWN")) {
            var fileExists = Utils.fileExists(uri);
            levelMap.put(level, fileExists ? "FILE_EXISTS" : "FILE_NOT_EXISTS");
            return fileExists;
        }
        else if (status.equals("FILE_EXISTS")) {
            return true;
        }
        else if (status.equals("FILE_NOT_EXISTS")) {
            return false;
        }

        return false;
    }

    private String extractRecordNameFrom(String uriStr) {
        var startInd = 1 + uriStr.lastIndexOf("/");
        var recordName = uriStr.substring(startInd);
        if (recordName.isBlank()) {
            throw new RuntimeException("Unexpected result when extract record name.");
        }
        return recordName;
    }

    private String extractRecordPathFrom(String uriStr) {
        var index = uriStr.lastIndexOf(dbInfo.localUri().toString());

        if (index < 0) {
            index = dbInfo.remoteUri().toString().length()
                    + uriStr.lastIndexOf(dbInfo.remoteUri().toString());
        }
        else {
            index += dbInfo.localUri().toString().length();
        }

        return uriStr.charAt(index) != File.separator.charAt(0)
                ? uriStr.substring(index) : uriStr.substring(index + 1);
    }
}
