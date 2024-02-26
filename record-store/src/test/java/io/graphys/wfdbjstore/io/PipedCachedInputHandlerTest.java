package io.graphys.wfdbjstore.io;

import io.graphys.wfdbjstore.BaseTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PipedCachedInputHandlerTest extends BaseTest {
    private String inName = "https://physionet.org/files/mimic4wdb/0.1.0/waves/p100/p10014354/81739927/81739927_0008e.dat";
    //private String inName = "file:///home/nhtri/wfdb-jstore/record-store/data/input/81739927_0008e.dat";
    private String cachedName = "data/output/cached_81739927_0008e.dat";
    private String logName = "data/logs/81739927_0008e";
    private ExecutorService executorService;

    @BeforeEach
    void doBeforeTest() {
        executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    @AfterEach
    void doAfterTest() throws InterruptedException {
        if (executorService.isShutdown()) {
            return;
        }
        executorService.shutdown();
        if (executorService.awaitTermination(20, TimeUnit.SECONDS)) {
            logger.info("Executor service gracefully terminated.");
        }
        else {
            logger.warn("Executor service terminated before threads end.");
        }
    }


    @Test
    void testPairRW() throws MalformedURLException {
        var handler = new CacheInputCoordinatorImpl(
                URI.create(inName).toURL(), new File(cachedName), executorService);

        var start = Instant.now().toEpochMilli();
        //handler.awaitRequestCache(true);
        handler.prepareCache(false);
        //threadSleep(1);
        try (var out = new PrintWriter(logName);
             //var fileIn = new FileInputStream(cachedName);
             var pipedIn = handler.getInput(CacheInputCoordinator.InputBackend.CACHE_FILE_STREAM);
             //var pipedIn = new PipedCachedInputStream(cachedName, null);
        ) {
            //var buffer = new byte[64];
            //var bytesRead = in.read(buffer);
            //logger.info(new String(buffer, 0, bytesRead));
            //fileIn.transferTo(fileOut);
            //pipedIn.transferTo(fileOut);

            /*var inChannel = Channels.newChannel(fileIn);
            var outChannel = fileOut.getChannel();
            outChannel.transferFrom(inChannel, 0, Long.MAX_VALUE);*/

            var buffer = new byte[4096];
            var bytesRead = 0; var totalByteRead = 0;
            out.println("start computational process...");
            out.println(String.format("After %d ms.", Instant.now().toEpochMilli() - start));
            while ((bytesRead = pipedIn.read(buffer)) != -1) {
                doHeavyComputationOnBytes(buffer);
                totalByteRead += bytesRead;
                out.println(String.format(
                        "Next %d bytes after %d ms.", bytesRead, Instant.now().toEpochMilli() - start));
            }
            logger.info("Total bytes read: {} bytes", totalByteRead);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPairRW_concurrent() throws InterruptedException, MalformedURLException {
        var cachedFile = new File(cachedName);
        if (cachedFile.exists()) {
            cachedFile.delete();
        }
        var handler = new CacheInputCoordinatorImpl(
                URI.create(inName).toURL(), new File(cachedName), executorService);

        int bufSize = 8 * 512;
        int bytesThreshold = 20 * 1024;
        var readerIdGen = new AtomicInteger(0);
        var firstThread = new AtomicBoolean(true);

        var readerGotFirstBytes = Collections.synchronizedList(new LinkedList<Long>());
        var readerThresholdReached = Collections.synchronizedList(new LinkedList<Long>());
        var readerCompleted = Collections.synchronizedList(new LinkedList<Long>());

        executorService = Executors.newVirtualThreadPerTaskExecutor();
        for (int i = 0; i < 40_000; i++) {
            //if (i == 1) threadSleep(1000);
            executorService.submit(() -> {
                var start = Instant.now();
                int readerId = readerIdGen.getAndIncrement();
                handler.prepareCache(false);

                //logger.info("Reader {} have requested cache after {} ms.", readerId, Duration.between(start, Instant.now()).toMillis());
                try (var in = handler.getInput(CacheInputCoordinator.InputBackend.CACHE_FILE_STREAM)) {
                    int totalBytesRead = 0, bytesRead;
                    var thresholdReachedLog = false;
                    var firstBytesReadLog = false;
                    var readBytes = new byte[bufSize];

                    //logger.info("Reader {} have stream after {} ms.", readerId, Duration.between(start, Instant.now()).toMillis());

                    if (firstThread.getAndSet(false)) {
                        //threadSleep(2000);
                    }

                    while ((bytesRead = in.read(readBytes)) != -1) {
                        totalBytesRead += bytesRead;
                        //doHeavyComputationOnBytes(readBytes);
                        if (totalBytesRead >= bytesThreshold && !thresholdReachedLog) {
                            /*logger.info("Reader {} read {} bytes after {} ms.",
                                    readerId, totalBytesRead, Duration.between(start, Instant.now()).toMillis());*/
                            readerThresholdReached.add(Duration.between(start, Instant.now()).toMillis());
                            thresholdReachedLog = true;
                        }

                        if (!firstBytesReadLog) {
                            readerGotFirstBytes.add(Duration.between(start, Instant.now()).toMillis());
                            firstBytesReadLog = true;
                        }
                        /*logger.info("Reader {} read next {} bytes after {} ms.",
                                readerId, bytesRead, Duration.between(start, Instant.now()).toMillis());*/
                    }
                    /*logger.info("Reader {} completely read {} bytes after {} ms.",
                            readerId, totalBytesRead, Duration.between(start, Instant.now()).toMillis());*/
                    readerCompleted.add(Duration.between(start, Instant.now()).toMillis());
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();
        if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
            logger.info("Readers still uncompleted but es terminated.");
        }

        var gotFirstBytesStats = readerGotFirstBytes.stream().mapToLong(Long::longValue).summaryStatistics();
        var thresholdStats = readerThresholdReached.stream().mapToLong(Long::longValue).summaryStatistics();
        var completionStats = readerCompleted.stream().mapToLong(Long::longValue).summaryStatistics();
        logger.info("Reader get first {} bytes - stats {}", bufSize, gotFirstBytesStats);
        logger.info("Reader threshold ({} bytes) reached - stats: {}",bytesThreshold, thresholdStats);
        logger.info("Reader completed - stats: {}", completionStats);

        var fastestThreshold = thresholdStats.getMin() * 2;
        var fastestReaders = readerThresholdReached.stream().filter(l -> l < fastestThreshold).count();
        logger.info("Readers reached threshold before {} ms: {}", fastestThreshold, fastestReaders);
    }



}













