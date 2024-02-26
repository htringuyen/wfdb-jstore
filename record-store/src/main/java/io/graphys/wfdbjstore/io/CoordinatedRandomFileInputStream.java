package io.graphys.wfdbjstore.io;

import io.graphys.wfdbjstore.exception.ReadingInterruptedException;
import org.kc7bfi.jflac.io.RandomFileInputStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class CoordinatedRandomFileInputStream extends RandomFileInputStream {
    private final CacheInputCoordinatorImpl.Reading readingHandler;

    public CoordinatedRandomFileInputStream(File file, CacheInputCoordinatorImpl.Reading readingHandler) throws FileNotFoundException  {
        super(file);
        this.readingHandler = readingHandler;
    }

    public CoordinatedRandomFileInputStream(String fileName, CacheInputCoordinatorImpl.Reading readingHandler) throws FileNotFoundException  {
        super(fileName);
        this.readingHandler = readingHandler;
    }

    public CoordinatedRandomFileInputStream(RandomAccessFile randomFile, CacheInputCoordinatorImpl.Reading readingHandler) {
        super(randomFile);
        this.readingHandler = readingHandler;
    }

    // re-impl: wait next bytes avail instead of return -1 when eof reached
    @Override
    public int read() throws IOException {
        var readByte = randomFile.read();
        var shouldRetry = readingHandler != null && readByte == -1;

        while (shouldRetry) {
            try {
                var availNext = readingHandler.waitNextBytes();
                if (availNext) {
                    shouldRetry = (readByte = randomFile.read()) == -1;
                }
                else {
                    shouldRetry = false;
                }
            }
            catch (InterruptedException e) {
                throw new ReadingInterruptedException(e.getMessage());
            }
        }
        return readByte;
    }

    // re-impl: wait next bytes avail instead of return -1 when eof reached
    @Override
    public void close() throws IOException {
        super.close();
        readingHandler.readerCompleted();
    }

    // re-impl: wait next bytes avail instead of return -1 when eof reached
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        var bytesRead = randomFile.read(b, off, len);
        var shouldRetry = readingHandler != null && bytesRead == -1;

        while (shouldRetry) {
            try {
                var availNext = readingHandler.waitNextBytes();
                if (availNext) {
                    shouldRetry = (bytesRead = randomFile.read(b, off, len)) == -1;
                }
                else {
                    shouldRetry = false;
                }
            }
            catch (InterruptedException e) {
                throw new ReadingInterruptedException(e.getMessage());
            }
        }

        return bytesRead;
    }

    // re-impl: wait next bytes avail instead of return -1 when eof reached
    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length );
    }
}

































