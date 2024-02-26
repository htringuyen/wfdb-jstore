package io.graphys.codectest.justflac;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.util.StopWatch;

import java.util.concurrent.TimeUnit;

public class BaseTest {
    private static StopWatch stopWatch = new StopWatch();
    protected static final Logger logger = LogManager.getLogger(FlacAnalyserTest.class);

    @BeforeEach
    void logEachStartTime() {
        stopWatch.start();
    }

    @AfterEach
    void logEachStopTime() {
        stopWatch.stop();
        logger.info("Execution completed after {} ms.", stopWatch.getTotalTime(TimeUnit.MILLISECONDS));
    }
}
