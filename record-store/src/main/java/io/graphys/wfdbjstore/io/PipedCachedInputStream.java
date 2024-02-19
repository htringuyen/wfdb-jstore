package io.graphys.wfdbjstore.io;

import java.io.*;

public class PipedCachedInputStream extends InputStream {
    private FileInputStream finStream;
    private PipedCachedInputHandler.Reading handler;

    public PipedCachedInputStream(String fileName, PipedCachedInputHandler.Reading handler) throws FileNotFoundException {
        this.finStream = new FileInputStream(fileName);
        this.handler = handler;
    }

    public PipedCachedInputStream(File file, PipedCachedInputHandler.Reading handler) throws FileNotFoundException {
        this.finStream = new FileInputStream(file);
        this.handler = handler;
    }

    @Override
    public int read() throws IOException {
        var readByte = finStream.read();
        var shouldRetry = handler != null && readByte == - 1;

        while (shouldRetry) {
            try {
                var availNext = handler.waitNextBytes();
                if (availNext) {
                    shouldRetry = (readByte = finStream.read()) == -1;
                }
                else {
                    shouldRetry = false;
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                shouldRetry = false;
            }
        }
        return readByte;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        var bytesRead = finStream.read(b, off, len);
        var shouldRetry = handler != null && bytesRead == -1;

        while (shouldRetry) {
            try {
                var availNext = handler.waitNextBytes();
                if (availNext) {
                    shouldRetry = (bytesRead = finStream.read(b, off, len)) == -1;
                }
                else {
                    shouldRetry = false;
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                shouldRetry = false;
            }
        }

        return bytesRead;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length  );
    }

    @Override
    public void close() throws IOException {
        finStream.close();
    }

}
