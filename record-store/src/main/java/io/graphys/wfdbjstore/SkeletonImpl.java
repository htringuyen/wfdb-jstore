package io.graphys.wfdbjstore;

import io.graphys.wfdbjstore.exception.SkeletonBuildFailedException;
import io.graphys.wfdbjstore.exception.SkeletonScanFailedException;
import io.graphys.wfdbjstore.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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


public class SkeletonImpl implements Skeleton {
    private static final Logger logger = LogManager.getLogger();

    private static final String PATH_SEGMENT_LIST_FN = "RECORDS";
    private static final String RECORD_PATHS_FN = "RECORD_PATHS.csv";
    private static final String SEGMENT_BOUNDARY_MARK = ">>";

    private final DatabaseInfo dbInfo;

    private final Builder builder;
    private final Scanner scanner;

    SkeletonImpl(DatabaseInfo dbInfo) {
        this.dbInfo = dbInfo;
        this.scanner = new Scanner();
        this.builder = new Builder();
    }

    @Override
    public void build() {
        builder.buildSkeleton();
    }

    @Override
    public boolean isBuilt() {
        return scanner.scanExisted();
    }

    @Override
    public PathInfo[] findAllPathInfo() {
        return scanner.findAllPathInfo();
    }

    @Override
    public PathInfo findPathInfoOf(String recordName) {
        return scanner.findPathInfoOf(recordName);
    }

    @Override
    public String[] findPathSegments(int ordinal) {
        return scanner.findPathSegments(ordinal);
    }

    public class Scanner {
        protected Scanner() {

        }

        private boolean scanExisted() {
            if (!Files.exists(Path.of(dbInfo.localHome().resolve(RECORD_PATHS_FN)))) {
                return false;
            }
            try (var in = new java.util.Scanner(Path.of(dbInfo.localHome().resolve(RECORD_PATHS_FN)))) {
                in.useDelimiter("\n");
                return in
                        .tokens()
                        .parallel()
                        .map(row -> {
                            var tokens = row.split(",");
                            return Arrays
                                    .stream(tokens, 1, tokens.length)
                                    .map(String::strip)
                                    .collect(Collectors.joining(""));
                        })
                        .allMatch(s -> Files.exists(Path.of(dbInfo.localHome().resolve(s))));
            }
            catch (IOException e) {
                logger.debug("stacktrace for debug", e);
                throw new SkeletonScanFailedException(e.getMessage());
            }

        }

        protected PathInfo[] findAllPathInfo() {
            try (var in = new java.util.Scanner(Path.of(dbInfo.localHome().resolve(RECORD_PATHS_FN)))) {
                in.useDelimiter("\n");
                return in
                        .tokens()
                        .parallel()
                        .map(this::parsePathInfo)
                        .toArray(PathInfo[]::new);
            }
            catch (IOException e) {
                logger.debug("stacktrace for debug", e);
                throw new SkeletonScanFailedException(e.getMessage());
            }
        }

        protected PathInfo findPathInfoOf(String recordName) {
            if (recordName == null) {
                return null;
            }

            try (var in = new java.util.Scanner(Path.of(dbInfo.localHome().resolve(RECORD_PATHS_FN)))) {
                var pathInfo = in.tokens()
                        .parallel()
                        .filter(s -> s.startsWith(recordName + ","))
                        .map(this::parsePathInfo)
                        .findFirst()
                        .orElse(null);

                return pathInfo;
            }
            catch (IOException e) {
                logger.debug("stacktrace for debug", e);
                throw new SkeletonScanFailedException(e.getMessage());
            }
        }

        protected String[] findPathSegments(int level) {
            if (level < 1) {
                return null;
            }

            try (var in = new java.util.Scanner(Path.of(dbInfo.localHome().resolve(RECORD_PATHS_FN)))) {
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
                        .toArray(String[]::new);
            }
            catch (IOException e) {
                logger.debug("stacktrace for debug", e);
                throw new SkeletonScanFailedException(e.getMessage());
            }
        }

        private PathInfo parsePathInfo(String str) {
            var columns = str.split(",");
            if (columns[0].isBlank()
                    || columns.length < 2) {
                throw new SkeletonBuildFailedException("Error when reading file " + RECORD_PATHS_FN);
            }
            return PathInfo
                    .builder()
                    .dbInfo(dbInfo)
                    .name(columns[0])
                    .pathSegments(Arrays.copyOfRange(columns, 1, columns.length))
                    .build();
        }
    }

    public class Builder {

        protected Builder() {

        }

        protected void buildSkeleton() {
            var recordPaths = Collections.synchronizedList(new LinkedList<String>());
            var pathAppenders = new ConcurrentHashMap<URI, List<String>>();
            pathAppenders.put(dbInfo.localHome(), Collections.synchronizedList(new LinkedList<>()));

            var dir = new File(dbInfo.localHome());
            if (! dir.isDirectory() && ! dir.mkdirs()) {
                throw new RuntimeException("Error when creating directory: " + dir);
            }
            buildSkeletonHelper(dbInfo.remoteHome(), dbInfo.localHome(), "",
                    pathAppenders, 1, new HashMap<Integer, String>(), recordPaths);


            try (var out = new PrintWriter(
                    new File(dbInfo.localHome().resolve(RECORD_PATHS_FN)))) {
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
                        "error when open file: " + dbInfo.remoteHome().resolve(RECORD_PATHS_FN));
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
            try (var in = new java.util.Scanner(remoteUri.resolve(PATH_SEGMENT_LIST_FN).toURL().openStream())) {
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
    }

}















































