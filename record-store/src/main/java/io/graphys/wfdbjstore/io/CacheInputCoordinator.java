package io.graphys.wfdbjstore.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Given an input source represented by an {@link URL}, provide multiple
 * ways, {@link InputStream}s, to read the input, some of them are potentially
 * much more performant than reading from the source directly.
 * <br>
 * <br>
 * Under the hood, implementations of this interface should use a local file to cache the source input and allow concurrent reading the data
 * whenever new bytes written, not when all bytes written. For data integrity, the implementations must follow a rule: at the time of constructing
 * new input stream from cache file, the cache is considered to be available if cache file exists, even empty file, and if the cache is available,
 * the input stream is ensured to behave as if the cache file have completely written.
 * <br>
 * <br>
 * Although the source URL can point to a local file, http address or anything convertable to input stream, however,
 * the local file caching mechanism may have no advantage in cases the source input stream itself can be read
 * concurrently at higher speed that local file input stream.
 * <br>
 * <br>
 * The new input stream returned from {@link CacheInputCoordinator#getInput(InputBackend)} if of one of the following
 * types of backend:
 * <ul>
 *     <li>
 *         {@link InputBackend#CACHE_FILE_STREAM}: the data stream is derived from the cache file which the source stream transferred data
 *         into when calling methods {@link CacheInputCoordinator#prepareCache} or {@link CacheInputCoordinator#awaitPrepareCache}. If the cache
 *         file does not exists at the time of constructing new stream, then an exception will be thrown.
 *     </li>
 *     <li>
 *         {@link InputBackend#CACHE_FILE_RANDOM_ACCESS}: similar to CACHE_FILE_STREAM but using RandomAccessFile, instead stream, under the hood.
 *     </li>
 *     <li>
 *         {@link InputBackend#SOURCE_ORIGINAL_STREAM}: construct new stream directly from original URL.
 *     </li>
 * </ul>
 */
public interface CacheInputCoordinator {
    /**
     * The supported input backends.
     */
    enum InputBackend {
        /**
         * The backend is a stream constructed from cache file.
         */
        CACHE_FILE_STREAM,

        /**
         * The backend is a stream that wraps a {@link java.io.RandomAccessFile}.
         */
        CACHE_FILE_RANDOM_ACCESS,

        /**
         * The backend is the stream get directly from original URL.
         */
        SOURCE_ORIGINAL_STREAM
    }

    /**
     * Return the source URL.
     * @return the source URL
     */
    URL getSource();

    /**
     * Return the File instance of the cache file.
     * @return the File instance of the cache file
     */
    File getCacheFile();

    /**
     * Ensuring the cache file is available. If the cache available and the refresh param not set then do nothing,
     * otherwise create new Thread and write source data into the file from the first byte position.
     * @param refresh if true, rewrite the cache file anyway, even if the cache available
     */
    void prepareCache(boolean refresh);

    /**
     * Similar to {@link CacheInputCoordinator#prepareCache}, but instead of creating new Thread for writing,
     * it uses current Thread and, hence, this thread has to wait for writing completed.
     * @param refresh if true, rewrite the cache file anyway, even if the cache available
     */
    void awaitPrepareCache(boolean refresh);

    /**
     * Return input stream for reading data from source URL, either directly or via cached file.
     * @param ib the type of input backend will be used under the hood of the returned stream
     * @return the stream for reading data
     * @throws IOException if failed to construct the input stream, either because cache file not
     * available but required or unable to open input stream from source URL
     */
    InputStream getInput(InputBackend ib) throws IOException;

    /**
     * Return the number of readers currently depending on this object to read data.
     * @return the number of readers currently depending on this object to read data
     */
    int getNumReaders();

    /**
     * Delete the cache file if exists.
     * @return true if the cache file exists and be deleted, false otherwise
     */
    boolean clearCache();
}


















