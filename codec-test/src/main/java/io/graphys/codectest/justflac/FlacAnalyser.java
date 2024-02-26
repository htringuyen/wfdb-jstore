package io.graphys.codectest.justflac;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

import org.kc7bfi.jflac.FLACDecoder;
import org.kc7bfi.jflac.FrameListener;
import org.kc7bfi.jflac.frame.Frame;
import org.kc7bfi.jflac.metadata.Metadata;



public class FlacAnalyser implements FrameListener, AutoCloseable {
    static final ScopedValue<String> IN_NAME = ScopedValue.newInstance();
    private static final Logger logger = LogManager.getLogger(FlacAnalyser.class);

    private int frameNum = 0;
    private String inPath;
    private String outPath;
    private InputStream inStream;
    private PrintWriter dataOut;
    private PrintWriter headerOut;

    public FlacAnalyser(String inPath, String outPath) throws IOException {
        this.inPath = inPath;
        this.outPath = outPath;

        inStream = new FileInputStream(new File(inPath));
        dataOut = new PrintWriter(outPath.endsWith(".frame") ? outPath : outPath + ".frame");

        headerOut = new PrintWriter(!outPath.endsWith(".csv") ?
                outPath + ".hea" : outPath.substring(0, outPath.length() - 4) + ".hea");
    }

    public void decode() throws IOException {
        var flacDecoder = new FLACDecoder(inStream);
        flacDecoder.addFrameListener(this);
        flacDecoder.decode();
    }

    @Override
    public void processMetadata(Metadata metadata) {
        headerOut.println(metadata);
    }

    @Override
    public void processFrame(Frame frame) {
        frameNum++;
        dataOut.println(frameNum + " " + frame);
    }

    @Override
    public void processError(String msg) {
        logger.error(msg);
    }

    @Override
    public void close() {
        try {
            inStream.close();
            dataOut.close();
            headerOut.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Long sumAllSignals() {
        try (
                var inStream = new FileInputStream(IN_NAME.get());
        ) {
            var decoder = new FLACDecoder(inStream);
            decoder.readMetadata();
            var shouldContinue = true;

            long sumAll = 0;
            while (shouldContinue) {
                var frame = decoder.readNextFrame();

                if (frame == null && decoder.isEOF()) {
                    shouldContinue = false;
                }
                else if (frame == null && !decoder.isEOF()) {
                    logger.warn("Bad frame found!");
                }
                else {
                    var blockSize = frame.header.blockSize;
                    var data = decoder.getChannelData();
                    sumAll += IntStream
                            .range(0, blockSize)
                            .map(i -> Arrays.stream(data)
                                    .filter(Objects::nonNull)
                                    .map(ch -> ch.getOutput()[i])
                                    .mapToInt(v -> v)
                                    .sum()
                            )
                            .sum();
                }
            }
            return sumAll;
        }
        catch (IOException e) {
            throw new RuntimeException();
        }
    }

}
