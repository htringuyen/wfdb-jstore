package io.graphys.codectest.justflac;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;

public class FlacPlayerTest extends BaseTest {

    @Test
    void testSumAllSignals() {
        String inName = "data/fin/81739927_0008e.dat";

        ScopedValue.where(FlacContext.INPUT_NAME, inName)
                .run(() -> {
                    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                        var tasks = new LinkedList<StructuredTaskScope.Subtask<Long>>();
                        for (int i = 0; i < 10; i++) {
                            var flacPlayer = new FlacPlayer();
                            tasks.add(scope.fork(flacPlayer::sumAllSignals));
                        }
                        scope
                                .join()
                                .throwIfFailed();
                        tasks
                                .stream()
                                .map(StructuredTaskScope.Subtask::get)
                                .forEach(v -> logger.info("Sum All: {}", v));
                    }
                    catch (InterruptedException | ExecutionException | FileNotFoundException e) {
                        throw new RuntimeException();
                    }
                });
    }

    @Test
    void testSumAllSignalsStepping() {
        String inName = "data/fin/81739927_0008e.dat";
        var nSteps = 0;

        ScopedValue.where(FlacContext.INPUT_NAME, inName)
                .run(() -> {
                    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                        var tasks = new LinkedList<StructuredTaskScope.Subtask<Long>>();
                        for (int i = 0; i < 10; i++) {
                            tasks.add(scope.fork(() -> FlacPlayer.sumAllSignalsStepping(nSteps)));
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
                        e.printStackTrace();
                        throw new RuntimeException();
                    }
                });
    }
}
