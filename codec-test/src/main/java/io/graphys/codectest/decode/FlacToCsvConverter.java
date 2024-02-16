package io.graphys.codectest.decode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jflac.Constants;
import org.jflac.FLACDecoder;
import org.jflac.FrameDecodeException;
import org.jflac.FrameListener;
import org.jflac.frame.Frame;
import org.jflac.metadata.Metadata;

import java.io.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FlacToCsvConverter implements FrameListener {
    private static final Logger logger = LogManager.getLogger(FlacToCsvConverter.class);

    private int frameNum = 0;
    private String inPath;
    private String outPath;
    private InputStream inStream;
    private PrintWriter csvOut;

    public FlacToCsvConverter(String inPath, String outPath) throws IOException {
        this.inPath = inPath;
        this.outPath = outPath;

        inStream = new FileInputStream(new File(inPath));
        csvOut = new PrintWriter(outPath.endsWith(".csv") ? outPath : outPath + ".csv");
    }

    @Override
    public void processMetadata(Metadata metadata) {

    }

    @Override
    public void processFrame(Frame frame) {

    }

    @Override
    public void processError(String msg) {

    }



    public static void toCsv(String inFile, String outFile) {
        try (
                var inStream = new FileInputStream(inFile);
                var out = new PrintWriter(outFile.endsWith(".csv") ? outFile : outFile + ".csv");
        ) {
            var decoder = new FLACDecoder(inStream);
            decoder.readMetadata();
            var shouldContinue = true;

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
                    IntStream.range(0, blockSize)
                            .mapToObj(i -> Arrays.stream(data)
                                    .filter(Objects::nonNull)
                                    .map(ch -> String.valueOf(ch.getOutput()[i]))
                                    .collect(Collectors.joining(","))
                            )
                            .forEach(out::println);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}





























