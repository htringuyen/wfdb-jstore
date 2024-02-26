package io.graphys.wfdbjstore;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.StopWatch;
import wfdb.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@ContextConfiguration(classes = TestConfiguration.class)
@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:test.properties")
public class BaseTest {
    protected final Logger logger = LogManager.getLogger(BaseTest.class);

    protected StopWatch stopWatch = new StopWatch();

    protected Random random = new Random();

    protected WfdbManager wfdbManager = WfdbManager.get();

    @Value("${path.local.home_dir}")
    protected String LOCAL_ROOT;

    @Value("${path.remote.home_dir}")
    protected String REMOTE_ROOT;

    @Value("${cache.path.limit}")
    protected int PATH_CACHE_LIMIT;

    @Value("${cache.record.limit}")
    protected int RECORD_CACHE_LIMIT;

    @BeforeEach
    void logStartTime() {
        stopWatch.start();
        wfdb.setwfdb(String.join(";", wfdbManager.getNativeWfdbPaths()));
        wfdb.wfdbquit();
        logger.info("wfdb paths: {}", wfdb.getwfdb());
    }

    @AfterEach
    void logStopTime() {
        stopWatch.stop();
        logger.info("Executed in " + stopWatch.getTotalTime(TimeUnit.MILLISECONDS) + " ms.");
    }

    protected void threadSleep(int millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected int randomInt(int bound) {
        return random.nextInt(bound);
    }

    protected void doHeavyComputationOnBytes(byte[] bytes) {
        double sum = 0;
        for (byte aByte : bytes) {
            sum += Math.pow(aByte, 5);
        }
    }
}