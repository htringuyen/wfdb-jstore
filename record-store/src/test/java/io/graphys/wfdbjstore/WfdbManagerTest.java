package io.graphys.wfdbjstore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class WfdbManagerTest {
    private WfdbManager manager;

    @BeforeEach
    void doBeforeTest() {
        this.manager = WfdbManager.get();
    }

    @Test
    void testInitiatedSuccessful() {
        assertNotNull(manager);
    }
}
