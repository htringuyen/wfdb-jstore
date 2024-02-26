package io.graphys.wfdbjstore;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class WfdbStoreImplTest extends BaseTest {

    private WfdbStore wfdbStore;

    @BeforeEach
    void initObjects() {
        assertEquals(1, wfdbManager.getNumDatabases());
        wfdbStore = wfdbManager.getWfdbStore("mimic4wdb", "0.1.0");
        assertNotNull(wfdbStore);
    }

    @Test
    void testBuild() throws IOException {
        FileUtils.deleteDirectory(new File(wfdbStore.getDbInfo().localHome()));
        assertFalse(wfdbStore.isBuilt());
        wfdbStore.build();
        assertTrue(wfdbStore.isBuilt());
    }

    @Test
    void testFindPathInfoByRecordName() {
        var pathInfo = wfdbStore.findPathInfoOf("81739927");
        assertEquals("81739927", pathInfo.getRecordName());
        assertEquals("waves/p100/p10014354/", pathInfo.getPathSegment(0));
        assertEquals("81739927/", pathInfo.getPathSegment(1));
        assertEquals("waves/p100/p10014354/81739927/", pathInfo.getRelativeDir());
        assertEquals("mimic4wdb/0.1.0/waves/p100/p10014354/81739927/", pathInfo.getAbsoluteDir());
        assertEquals("mimic4wdb/0.1.0/waves/p100/p10014354/81739927/81739927", pathInfo.getRecordPath());
    }

    @Test
    void testFindAllPathInfo() {
        var pathInfo = wfdbStore.findAllPathInfo();
        assertEquals(pathInfo.length, 200);
    }
}













































