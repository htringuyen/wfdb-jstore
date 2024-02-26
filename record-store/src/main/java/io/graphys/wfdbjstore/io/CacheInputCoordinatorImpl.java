package io.graphys.wfdbjstore.io;

import io.graphys.wfdbjstore.exception.CacheWriterFailedException;

import java.io.*;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CacheInputCoordinatorImpl implements CacheInputCoordinator {
    // the coordinated object reading
    private Reading reading;

    // the coordinated object writing
    private Writing writing;

    // executor service for running writing tanks
    private ExecutorService executorService;

    public CacheInputCoordinatorImpl(URL source, File cacheFile, ExecutorService es, int bufSize) {
        this.executorService = es;
        this.reading = new Reading(source, cacheFile);
        this.writing = new Writing(source, cacheFile, reading, bufSize);
    }

    public CacheInputCoordinatorImpl(URL source, File cacheFile, ExecutorService es) {
        this(source, cacheFile, es, Writing.DEFAULT_BUF_SIZE);
    }

    @Override
    public URL getSource() {
        return writing.source;
    }

    @Override
    public File getCacheFile() {
        return writing.cacheFile;
    }

    @Override
    public int getNumReaders() {
        return reading.getNumReaders();
    }

    @Override
    public void prepareCache(boolean refresh) {
        writing.writeToCache(executorService);
    }

    @Override
    public void awaitPrepareCache(boolean refresh) {
        if (!writing.writeToCache()) {
            writing.pseudoWriting();
        }
    }

    @Override
    public InputStream getInput(InputBackend inputBackend) throws IOException {
        return reading.getReaderOf(inputBackend);
    }

    @Override
    public boolean clearCache() {
        writing.prepareWritingLock.lock();
        try {
            return reading.getNumReaders() == 0 && !writing.cacheFile.delete();
        } finally {
            writing.prepareWritingLock.unlock();
        }

    }

    class Reading {
        // this lock used for coordination of reader (opened input stream) waiting for new bytes
        private final ReentrantLock readLock = new ReentrantLock();
        private final Condition nextBytesAvail = readLock.newCondition();

        // the writing partner, one reading pair with one and only one writing and via versa
        private Writing writing;

        // count number of readers depending on this object for reading
        private final AtomicInteger numReaders = new AtomicInteger(0);

        // source input url
        private final URL source;

        // the file where cache is stored
        private final File cacheFile;

        private Reading(URL source, File cacheFile) {
            this.source = source;
            this.cacheFile = cacheFile;
        }

        // wait next byte avail in readLock only if there are writing thread
        boolean waitNextBytes() throws InterruptedException {
            readLock.lock();
            try {
                // check if there is writing thread
                if (!writing.isActive()) return false;

                nextBytesAvail.await();
                return true;
            } catch (InterruptedException e) {
                throw new InterruptedException(e.getMessage());
            } finally {
                readLock.unlock();
            }
        }

        // this method would be used by readers after reading completed
        void readerCompleted() {
            numReaders.getAndDecrement();
        }

        // return number of readers, this method would be used by writing to
        // determine if signaling is necessary
        private int getNumReaders() {
            return numReaders.get();
        }

        // register writing object paired with this reading
        private Reading register(Writing writing) {
            this.writing = writing;
            return this;
        }

        // get reader of given backend type
        private InputStream getReaderOf(InputBackend inputBackend) throws IOException {
            writing.prepareWritingLock.lock();
            try {
                if (writing.hadPreviouslyFailed()) {
                    throw new CacheWriterFailedException("Try to get input failed because previous cache writing is failed.");
                }
                switch (inputBackend) {
                    case SOURCE_ORIGINAL_STREAM:
                        return source.openStream();
                    case CACHE_FILE_STREAM:
                        numReaders.getAndIncrement();
                        return new CoordinatedInputStream(new FileInputStream(cacheFile), this);
                    case CACHE_FILE_RANDOM_ACCESS:
                        numReaders.getAndIncrement();
                        return new CoordinatedRandomFileInputStream(cacheFile, this);
                    default:
                        throw new IllegalArgumentException("Unsupported input backend: " + inputBackend);
                }
            } catch (FileNotFoundException e) {
                if (!inputBackend.equals(InputBackend.SOURCE_ORIGINAL_STREAM)) {
                    numReaders.getAndDecrement();
                }
                throw new IOException(e.getMessage());
            } finally {
                writing.prepareWritingLock.unlock();
            }
        }
    }

    class Writing {
        /**
         * Default buffer size, signal readers threads each time the buffer is filled and transferred to cached file.
         */
        public static final int DEFAULT_BUF_SIZE = 4 * 1024;

        // whether there is a thread writing to cache
        private final AtomicBoolean active = new AtomicBoolean(false);

        // whether previous writing to cache failed
        private final AtomicBoolean previouslyFailed = new AtomicBoolean(false);

        // size of buffer between source and cache file
        private final int bufSize;

        // ensure at most one thread writing cache at a time
        private final ReentrantLock writingLock = new ReentrantLock();

        // resolve contention when multiple threads want to read at a time and
        // currently there is no writing thread
        private final ReentrantLock prepareWritingLock = new ReentrantLock();

        // the reading paired with this writing
        private Reading reading = null;

        // the URL of the source where data obtained from
        private final URL source;

        // the cache file
        private final File cacheFile;

        private Writing(URL source, File cachedFile, Reading reading) {
            this(source, cachedFile, reading, DEFAULT_BUF_SIZE);
        }

        private Writing(URL source, File cacheFile, Reading reading, int bufSize) {
            this.source = source;
            this.cacheFile = cacheFile;
            this.bufSize = bufSize;
            this.reading = reading.register(this);
        }

        // is there any thread writing at this time
        private boolean isActive() {
            return active.get();
        }

        // whether previous writing failed
        private boolean hadPreviouslyFailed() {
            return previouslyFailed.get();
        }


        // prepare the writing, ensure that only one thread go to the next stage, i.e. to write cache
        private boolean prepareWriting() {
            prepareWritingLock.lock();
            try {
                if (isActive() || (!hadPreviouslyFailed() && cacheFile.isFile() && cacheFile.canRead())) {
                    return true;
                } else {
                    active.set(true);
                    if (!cacheFile.exists() && !cacheFile.createNewFile()) {
                        throw new CacheWriterFailedException(
                                "Cannot create cache file at: " + cacheFile.getAbsolutePath());
                    }
                    return false;
                }
            } catch (Exception e) {
                throw new CacheWriterFailedException("Exception when prepare writing", e);
            } finally {
                prepareWritingLock.unlock();
            }
        }

        // if executor service provided then write cache in new thread
        private void writeToCache(ExecutorService es) {
            es.submit(this::writeToCacheHelper);
        }

        // write to cache using this thread
        private boolean writeToCache() {
            return writeToCacheHelper();
        }

        // write to cache
        // possible bug: when writing failed, delete the cache file,
        // hopefully currently reading threads have an exception.
        // the previouslyFailed variable seem useless
        private boolean writeToCacheHelper() {
            prepareWritingLock.lock();
            try {
                if (isActive() || (!hadPreviouslyFailed() && cacheFile.isFile() && cacheFile.canRead())) {
                    return false;
                } else {
                    active.set(true);
                    if (!cacheFile.exists() && !cacheFile.createNewFile()) {
                        throw new CacheWriterFailedException(
                                "Cannot create cache file at: " + cacheFile.getAbsolutePath());
                    }
                    writingLock.lock();
                }
            } catch (Exception e) {
                throw new CacheWriterFailedException("Exception when prepare writing", e);
            } finally {
                prepareWritingLock.unlock();
            }

            boolean hasException = false;

            try (var in = source.openStream();
                 var out = new FileOutputStream(cacheFile)
            ) {
                var buffer = new byte[bufSize];
                int bytesRead;

                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);

                    if (reading.getNumReaders() > 0) {
                        reading.readLock.lock();
                        try {
                            reading.nextBytesAvail.signalAll();
                        } finally {
                            reading.readLock.unlock();
                        }

                    }
                }
            } catch (Exception e) {
                prepareWritingLock.lock();
                try {
                    previouslyFailed.set(hasException);
                    hasException = true;
                    active.set(false);
                    cacheFile.delete();
                } finally {
                    prepareWritingLock.unlock();
                }
                throw new CacheWriterFailedException("Error when writing to cache file.");
            } finally {
                active.set(false);
                reading.readLock.lock();
                previouslyFailed.set(hasException);
                try {
                    reading.nextBytesAvail.signalAll();
                } finally {
                    reading.readLock.unlock();
                }
                writingLock.unlock();
            }

            return true;
        }

        private void pseudoWriting() {
            writingLock.lock();
            try {
                // do nothing, only for ensuring that the writing clock is free
            } finally {
                writingLock.unlock();
            }
        }
    }
}
