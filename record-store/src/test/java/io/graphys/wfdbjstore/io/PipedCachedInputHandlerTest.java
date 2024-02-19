package io.graphys.wfdbjstore.io;

import io.graphys.wfdbjstore.BaseTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.Channels;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PipedCachedInputHandlerTest extends BaseTest {
    private String inName = "file:///home/nhtri/wfdb-jstore/record-store/data/input/Data8277.csv";
    private String cachedName = "data/output/cached_81739927n.csv";
    private String pipedName = "data/output/piped_81739927n.csv";
    private ExecutorService executorService;

    @BeforeEach
    void doBeforeTest() {
        executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    @AfterEach
    void doAfterTest() throws InterruptedException {
        executorService.shutdown();
        if (executorService.awaitTermination(60, TimeUnit.SECONDS)) {
            logger.info("Executor service gracefully terminated.");
        }
        else {
            logger.warn("Executor service terminated before threads end.");
        }
    }


    @Test
    void testPairRW() {
        var handler = new PipedCachedInputHandler(
                URI.create(inName), cachedName, executorService);

        try (var fileOut = new FileOutputStream(pipedName);
             var fileIn = new FileInputStream(cachedName);
             var pipedIn = new PipedCachedInputStream(cachedName, null);
        ) {
            //handler.awaitRequestCache(true);
            //var buffer = new byte[64];
            //var bytesRead = in.read(buffer);
            //logger.info(new String(buffer, 0, bytesRead));
            //fileIn.transferTo(fileOut);
            pipedIn.transferTo(fileOut);

            /*var inChannel = Channels.newChannel(fileIn);
            var outChannel = fileOut.getChannel();
            outChannel.transferFrom(inChannel, 0, Long.MAX_VALUE);*/

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}























