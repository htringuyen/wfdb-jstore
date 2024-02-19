package io.graphys.wfdbjstore.io;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PipedCachedInputHandler implements CachedInputHandler {
    private Reading reading = null;
    private Writing writing = null;
    private URI source;
    private File cachedFile;
    private ReentrantLock lock = new ReentrantLock();
    private ExecutorService executorService;

    public PipedCachedInputHandler(URI source, String cachedFile, ExecutorService es) {
        this(source, new File(cachedFile), es);
    }

    public PipedCachedInputHandler(URI source, File file, ExecutorService es) {
        try { source.toURL(); }
        catch (MalformedURLException e) {
            throw new IllegalArgumentException("Cannot convert the uri to url");
        }

        this.source = source;
        this.cachedFile = file;
        this.executorService = es;
    }

    @Override
    public URI getSource() { return source; }

    @Override
    public File getCachedFile() { return cachedFile; }

    @Override
    public void requestCache(boolean refresh) {
        requestCacheHelper(refresh, false);
    }

    @Override
    public void awaitRequestCache(boolean refresh) {
        requestCacheHelper(refresh, true);
    }

    @Override
    public InputStream getInput() throws IOException {
        lock.lock();
        try {
            if (reading == null) {
                reading = new Reading();
            }
            if (writing != null) {
                writing.registerReading(reading);
            }
        }
        finally { lock.unlock(); }

        return new PipedCachedInputStream(cachedFile, reading);
    }

    private boolean cacheAvail() {
        return cachedFile.isFile() && cachedFile.canRead();
    }

    private void requestCacheHelper(boolean refresh, boolean blocking) {
        lock.lock();
        if (cacheAvail() && !refresh) return;

        InputStream in = null;
        OutputStream out = null;
        try {
            if (writing == null) writing = new Writing();
            in = source.toURL().openStream();
            out = new FileOutputStream(cachedFile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally { lock.unlock();}

        var psIn = in; var psOut = out;

        if (blocking) {
            writing.startCaching(psIn, psOut);
        }
        else {
            executorService.submit(() -> writing.startCaching(psIn, psOut));
        }
    }


    class Reading {
        ReentrantLock lock = new ReentrantLock();
        Condition nextBytesAvail = lock.newCondition();
        private Writing writing;

        boolean waitNextBytes() throws InterruptedException {
            lock.lock();
            try {
                if (writing == null || !writing.isActive()) return false;
                nextBytesAvail.await();
                return true;
            }
            finally { lock.unlock(); }
        }

        void registerWriting(Writing writing) {
            this.writing = writing;
        }


    }

    class Writing {
        public static final int DEFAULT_BUF_SIZE = 512;
        private boolean active = false;
        private int bufSize;
        ReentrantLock lock = new ReentrantLock();
        private Reading reading = null;

        public Writing() { this.bufSize = DEFAULT_BUF_SIZE; }

        public Writing(int bufSize) { this.bufSize = bufSize; }

        public boolean isActive() { return active; }

        public void registerReading(Reading reading) { this.reading = reading; }

        public void startCaching(InputStream in, OutputStream out) {
            lock.lock();
            active = true;

            try (in; out) {
                var buffer = new byte[bufSize]; int bytesRead;

                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);

                    if (reading != null) {
                        reading.lock.lock();
                        try { reading.nextBytesAvail.signalAll(); }
                        finally { reading.lock.unlock(); }
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                lock.unlock();
                active = false;
            }
        }
    }
}
