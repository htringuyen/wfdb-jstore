package io.graphys.wfdbjstore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecordInfoRetrieverTest extends BaseTest {
    private RecordInfoRetriever recInfoRet;

    @BeforeEach
    void initiateRetriever() {
        var recStore = wfdbManager.getWfdbStore("mimic4wdb", "0.1.0");
        recInfoRet = recStore.getRecordInfoRetriever();
    }

    @Test
    void testGetAll() {
        var recInfoList = recInfoRet.getAll();
        assertEquals(200, recInfoList.size());
    }

    @Test
    void testGetByRecordName() {
        var recInfo = recInfoRet.getByRecordName("81739927");
        assertEquals("81739927", recInfo.getName());
        assertEquals("waves/p100/p10014354/", recInfo.getPathSegment(0));
        assertEquals("81739927/", recInfo.getPathSegment(1));
        assertEquals("waves/p100/p10014354/81739927/", recInfo.getRelativePath());
        assertEquals("mimic4wdb/0.1.0/waves/p100/p10014354/81739927/", recInfo.getFullPath());
        assertEquals("mimic4wdb/0.1.0/waves/p100/p10014354/81739927/81739927", recInfo.getEntryPoint());
    }
}
