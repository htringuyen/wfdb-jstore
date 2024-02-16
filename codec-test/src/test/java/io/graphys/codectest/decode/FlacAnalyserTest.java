package io.graphys.codectest.decode;

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class FlacAnalyserTest {

    @Test
    public void testDecode()  {
        String inPath = "data/fin/81739927_0001e.dat";
        String outPath = "data/fout/81739927_0001e";

        try (var decoder = new FlacAnalyser(inPath, outPath)) {
            decoder.decode();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
