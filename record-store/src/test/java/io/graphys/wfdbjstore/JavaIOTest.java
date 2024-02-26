package io.graphys.wfdbjstore;

import org.junit.jupiter.api.Test;
import org.kc7bfi.jflac.io.RandomFileInputStream;

import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class JavaIOTest extends BaseTest {
    @Test
    void testIO_PipingApproach() {
        try (
                InputStream sourceIn = new FileInputStream("data/input/81739927n.csv");
                var fileOut = new FileOutputStream("data/output/81739927n.csv");
                var pipedOut = new PipedOutputStream();
        ) {
            var broadcastIn = new PipedInputStream();
            pipedOut.connect(broadcastIn);

            broadcastData(broadcastIn);
            var buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = sourceIn.read(buffer)) != -1) {
                fileOut.write(buffer,0, bytesRead);
                pipedOut.write(buffer, 0, bytesRead);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testIO_FileIOApproach() {
        try
                /*(
                InputStream sourceIn = new FileInputStream("data/input/81739927n.csv");
                var fileOut = new FileOutputStream("data/output/81739927n.csv");
                )*/
        {

            /*var buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = sourceIn.read(buffer)) != -1) {
                fileOut.write(buffer,0, bytesRead);
            }

            fileOut.close();*/

            //var fileIn = new FileInputStream("data/output/81739927n.csv");
            broadcastData(null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void broadcastData(InputStream in) {
        var thread = Thread.startVirtualThread(() -> {
            try {
                //Thread.sleep(2000);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            try ( in;
                 var newIn = new FileInputStream("data/input/x81739927n.csv");
                 var fileOut = new FileOutputStream("data/output/broadcast_81739927n.csv");
            ) {
                var input = newIn;
                //Thread.sleep(2000);
                //var buffer = new byte[1];
                //int bytesRead;
                int readByte;
                while ((readByte = input.read()) != -1) {
                    fileOut.write(readByte);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    void testCopy() {
        try {
            //Thread.sleep(2000);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try (
              var newIn = new FileInputStream("data/input/x81739927n.csv");
              var fileOut = new FileOutputStream("data/output/broadcast_81739927n.csv");
        ) {
            var input = newIn;
            //Thread.sleep(2000);
            //var buffer = new byte[1];
            //int bytesRead;
            int readByte;
            while ((readByte = input.read()) != -1) {
                fileOut.write(readByte);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testConcurrentReadWrite() {
        try (
                var es = Executors.newVirtualThreadPerTaskExecutor();
                var out = new FileOutputStream("data/files/parallel-rw");
                var in = new FileInputStream("data/files/parallel-rw")
        ) {
            es.submit(() -> {
                for (int i = 0; i < 10; i++) {
                    threadSleep(100);
                    try {
                        var buffer = ("Line " + i + "\n").getBytes();
                        out.write(buffer);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            es.submit(() -> {
                threadSleep(1050);
                try {
                    var buffer = new byte[4096];
                    var bytesRead = in.read(buffer);
                    var msg = new String(buffer, 0, bytesRead);
                    logger.info(msg);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            });

            es.shutdown();
            es.awaitTermination(10, TimeUnit.SECONDS);

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testConcurrentReadWrite_RandomFileInputStream() {
        try (
                var es = Executors.newVirtualThreadPerTaskExecutor();
                var out = new FileOutputStream("data/files/parallel-rw");
                var in = new RandomFileInputStream("data/files/parallel-rw")
        ) {
            es.submit(() -> {
                for (int i = 0; i < 10; i++) {
                    threadSleep(100);
                    try {
                        var buffer = ("Line " + i + "\n").getBytes();
                        out.write(buffer);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            es.submit(() -> {
                threadSleep(350);
                try {
                    var buffer = new byte[4096];
                    var bytesRead = in.read(buffer);
                    var msg = new String(buffer, 0, bytesRead);
                    logger.info(msg);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            });

            es.shutdown();
            es.awaitTermination(10, TimeUnit.SECONDS);

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testConcurrentReadFile() throws InterruptedException {
        var inName = "data/input/81739927_0008e.dat";
        var es = Executors.newVirtualThreadPerTaskExecutor();
        for (int i = 0; i < 1000; i++) {
            es.submit(() -> {
                try (var in = new FileInputStream(inName)) {
                    var buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        //doHeavyComputationOnBytes(buffer);
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        es.shutdown();
        if (es.awaitTermination(10, TimeUnit.SECONDS)) {
            logger.info("Readers still uncompleted but es terminated.");
        }
        else {
            logger.info("All readers completed.");
        }
    }
}

























