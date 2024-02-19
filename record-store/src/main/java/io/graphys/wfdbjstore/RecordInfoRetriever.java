package io.graphys.wfdbjstore;

import io.graphys.wfdbjstore.RecordInfo;

import java.util.List;

/**
 * An interface for retrieving record info of the specific database.
 */
public interface RecordInfoRetriever {
    /**
     * Return list of info of all records of the database
     * @return List of all record info of the database
     */
    List<RecordInfo> getAll();

    /**
     * Return all path segments of given ordinal in the relative path of the record.
     * @param ordinal The ordinal of the segment in the relative path
     * @return All path segments of given ordinal
     */
    List<String> getPathSegments(int ordinal);

    /**
     * Return record info with given name
     * @param name Name of the record
     * @return RecordInfo object
     */
    RecordInfo getByRecordName(String name);
}
