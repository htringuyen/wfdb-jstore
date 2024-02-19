package io.graphys.wfdbjstore;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.StopWatch;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SkeletonTest extends BaseTest {
    private DatabaseInfo dbInfo;
    private StopWatch stopWatch = new StopWatch();

    @BeforeEach
    void doBeforeTest() {
        dbInfo = DatabaseInfo
                .builder()
                .name("mimic4wdb")
                .version("0.1.0")
                .localHome(URI.create(LOCAL_HOME))
                .remoteHome(URI.create(REMOTE_HOME))
                .build();

        //logger.info("db name: "+ dbInfo.name());
        //logger.info("remote home: " + dbInfo.remoteHome());
        //logger.info("remote uri: " + dbInfo.remoteUri());
        stopWatch.start();
    }

    @AfterEach
    void doAfterTest() {
        stopWatch.stop();
        var duration = stopWatch.getTotalTime(TimeUnit.MILLISECONDS);
        logger.info("Executed in " + duration + " ms.");
    }


    @Test
    void testInstantiationOfSkeletonAndBuildLocalFileStructure() {
        var skeleton = Skeleton.loadFor(dbInfo, true);
        assertNotNull(skeleton);
    }

    @Test
    void testWithPwaveDatabase() {
        var dbInfo = DatabaseInfo.builder()
                .name("pwave")
                .version("1.0.0")
                .localHome(URI.create(LOCAL_HOME))
                .remoteHome(URI.create(REMOTE_HOME))
                .build();

        var skeleton = Skeleton.loadFor(dbInfo, true);
        assertNotNull(skeleton);
    }

    @Test
    void testWithMimic4Ecg() {
        var dbInfo = DatabaseInfo.builder()
                .name("mimic-iv-ecg")
                .version("1.0")
                .localHome(URI.create(LOCAL_HOME))
                .remoteHome(URI.create(REMOTE_HOME))
                .build();

        var skeleton = Skeleton.loadFor(dbInfo, true);
        assertNotNull(skeleton);
    }

    @Test
    void testUriResolve() {
        var dbInfo = DatabaseInfo.builder()
                .name("pwave")
                .version("1.0.0")
                .localHome(URI.create(LOCAL_HOME))
                .remoteHome(URI.create(REMOTE_HOME))
                .build();

        var uri = dbInfo.remoteUri().resolve("100").resolve("RECORDS");

        logger.info("uri: " + uri);
    }
}
