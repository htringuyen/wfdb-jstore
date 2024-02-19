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

import java.util.concurrent.TimeUnit;

@ContextConfiguration(classes = TestConfiguration.class)
@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:test.properties")
public class BaseTest {
    protected static final Logger logger = LogManager.getLogger(BaseTest.class);

    protected StopWatch stopWatch = new StopWatch();

    protected WfdbManager wfdbManager = WfdbManager.get();

    @Value("${path.local.home_dir}")
    protected String LOCAL_HOME;

    @Value("${path.remote.home_dir}")
    protected String REMOTE_HOME;

    @Value("${cache.path.limit}")
    protected int PATH_CACHING_LIMIT;

    @Value("${cache.record.limit}")
    protected int RECORD_CACHING_LIMIT;

    @BeforeEach
    void logStartTime() {
        stopWatch.start();
        wfdb.setwfdb(LOCAL_HOME + ";" + REMOTE_HOME);
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
}