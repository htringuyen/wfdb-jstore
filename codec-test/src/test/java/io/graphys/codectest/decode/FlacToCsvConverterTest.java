package io.graphys.codectest.decode;

import org.junit.jupiter.api.Test;

public class FlacToCsvConverterTest {

    @Test
    void testToCsv() {
        /*String inPath = "data/fin/81739927_0008e.dat";
        String outPath = "data/fout/81739927_0008e";*/

        String inPath = "data/fin/81739927_0008e.dat";
        String outPath = "data/fout/81739927_0008e";

        FlacToCsvConverter.toCsv(inPath, outPath);
    }
}
