package io.graphys.codectest.wfdb;

import io.graphys.codectest.justflac.BaseTest;
import org.junit.jupiter.api.Test;

public class WfdbToCsvConverterTest extends BaseTest {
    @Test
    public void testConvertWfdbToCsv() {
        /*var record = "waves/p100/p10014354/81739927/81739927";
        var outName = "data/wfdb/output/81739927.csv";*/
        var record = "waves/p100/p10020306/83404654/83404654";
        var outName = "data/wfdb/output/83404654.csv";
        WfdbToCsvConverter.convertWfdbToCsv(record, outName, 250_000);
    }
}
