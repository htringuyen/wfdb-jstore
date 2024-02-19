package io.graphys.wfdbjstore;

import io.graphys.wfdbjstore.WfdbManager;
import io.graphys.wfdbjstore.WfdbManagerTest;
import io.graphys.wfdbjstore.WfdbStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertNotNull;

public class WfdbStoreTest extends BaseTest{
    private static final Logger logger = LogManager.getLogger(WfdbManagerTest.class);
    private WfdbStore wfdbStore;

    @BeforeEach
    void doBeforeTest() {
        var manager = WfdbManager.get();
        logger.info(manager.getProperties());
        wfdbStore = manager.getWfdbStore("mimic4wdb", "0.1.0");
    }

    @Test
    void testInitiatedSuccessful() {
        assertNotNull(wfdbStore);
    }
}
