package io.graphys.codectest.justflac;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;

import static io.graphys.codectest.justflac.FlacAnalyser.IN_NAME;

public class FlacAnalyserTest extends BaseTest{
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

    @Test
    public void testSumAllSignals() throws InterruptedException {
        String inName = "data/fin/81739927_0008e.dat";

        ScopedValue.where(IN_NAME, inName)
                .run(() -> {
                    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                        var tasks = new LinkedList<StructuredTaskScope.Subtask<Long>>();
                        for (int i = 0; i < 10; i++) {
                            tasks.add(scope.fork(FlacAnalyser::sumAllSignals));
                        }
                        scope
                                .join()
                                .throwIfFailed();
                        tasks
                                .stream()
                                .map(StructuredTaskScope.Subtask::get)
                                .forEach(v -> logger.info("Sum All: {}", v));
                    }
                    catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException();
                    }
                });
    }
}


























