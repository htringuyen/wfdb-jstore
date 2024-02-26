package io.graphys.wfdbjstore;

import lombok.Builder;

import java.net.URI;

/**
 * Containing information needed for constructing a Record object.
 */
@Builder
public class PathInfo {
    private DatabaseInfo dbInfo;
    private String name;
    private String[] pathSegments;

    /**
     * Return the name of the record, distinctive in context of given database.
     * @return Name of the record
     */
    public String getRecordName() {
        return name;
    }

    /**
     * Return info of the database the record belongs to.
     * @return The DatabaseInfo object
     */
    public DatabaseInfo getDbInfo() {
        return dbInfo;
    }

    /**
     * Return all segments that are segmented from record relative path.
     * @return Array of path segments
     */
    public String[] getPathSegments() {
        return pathSegments;
    }

    /**
     * Return segment of given ordinal in the relative path of the record. Segment ordinal is started at 0.
     * For example, the relative path '/seg1a/seg1b/seg2' of a record is segmented as
     * '/seg1a/seg1b' and 'seg2', then the assigned ordinal of each segment is 0 and 1, respectively.
     * @param ordinal Ordinal of the segment in relative path
     * @return The path segment of given ordinal in the relative path
     */
    public String getPathSegment(int ordinal) {
        return pathSegments[ordinal];
    }

    /**
     * Return the path of the record in context of the given database.
     * @return The path relative to given database
     */
    public String getRelativeDir() {
        var relativePath = String.join("/", pathSegments);
        return normalizePath(relativePath);
    }

    /**
     * Return global path of the record in context of all database.
     * @return The comprehensive path
     */
    public String getAbsoluteDir() {
        var relativePath = String.join("/", pathSegments);
        return normalizePath(dbInfo.name() + "/" + dbInfo.version() + "/" + relativePath);
    }

    /**
     * Return the entry point which points to the main header file of the record.
     * @return The entry point which points to the main header file of the record
     */
    public String getRecordPath() {
        return normalizePath(getAbsoluteDir() + "/" + getRecordName());
    }

    public URI formRemoteURIWith(String fileName) {
        return dbInfo.remoteHome().resolve(getRelativeDir() + fileName);
    }

    public URI formLocalURIWith(String fileName) {
        return dbInfo.localHome().resolve(getRelativeDir() + fileName);
    }


    private String normalizePath(String path) {
        var result = new StringBuilder();

        if (path.charAt(0) == '/') {
            result.append(path.substring(1));
        }

        result.append(path.replaceAll("[/]{2,}", "/"));
        /*if (result.charAt(result.length() - 1) == '/') {
            return result.substring(0, result.length() - 1);
        }*/

        return result.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PathInfo other) {
            return dbInfo.equals(other.dbInfo)
                    && name.equals(other.name);
        }
        return false;
    }
}

