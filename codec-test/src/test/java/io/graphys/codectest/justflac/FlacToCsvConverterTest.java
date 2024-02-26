package io.graphys.codectest.justflac;

import io.graphys.codectest.justflac.FlacToCsvConverter;
import org.junit.jupiter.api.Test;

public class FlacToCsvConverterTest {

    @Test
    void testFlacToCsv() {
        String inName = "data/fin/81739927_0001e.dat";
        String outName = "data/fout/81739927_0001e_v1.csv";

        var converter = new FlacToCsvConverter();
        converter.flacToCsv(inName, outName, 0);
    }

    @Test
    void testConvertFlacToCsv() {
        /*String inName = "data/fin/81739927_0001e.dat";
        String outName = "data/fout/81739927_0001e_v2.csv";*/

        String inName = "data/fin/83404654_0001e.dat";
        String outName = "data/fout/83404654_0001e_v2.csv";

        FlacToCsvConverter.convertFlacToCsv(inName, outName, 0);
    }

    @Test
    void testFlacToCsvWithSkipping() {
        int skips = 0;
        String inName = "data/fin/81739927_0001e.dat";
        String outName = "data/fout/81739927_0001e_v1_skipped_" + skips + ".csv";
        FlacToCsvConverter.convertFlacToCsv(inName, outName, skips);
    }

    @Test
    void testConvertFlacToCsvWithSkipping() {
        int skips = 1_000_000;
        String inName = "data/fin/81739927_0001e.dat";
        String outName = "data/fout/81739927_0001e_v2_skipped_" + skips + ".csv";
        /*String inName = "data/fin/83404654_0001e.dat";
        String outName = "data/fout/83404654_0001e_v2_skipped_" + skips + ".csv";*/
        FlacToCsvConverter.convertFlacToCsv(inName, outName, skips);
    }
}

























