package io.graphys.codectest.justflac;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kc7bfi.jflac.FLACDecoder;
import org.kc7bfi.jflac.FrameListener;
import org.kc7bfi.jflac.PCMProcessor;
import org.kc7bfi.jflac.frame.Frame;
import org.kc7bfi.jflac.io.RandomFileInputStream;
import org.kc7bfi.jflac.metadata.Metadata;
import org.kc7bfi.jflac.metadata.StreamInfo;
import org.kc7bfi.jflac.util.ByteData;

import java.io.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class FlacToCsvConverter implements FrameListener, PCMProcessor {
    private static final Logger logger = LogManager.getLogger(FlacToCsvConverter.class);

    private FLACDecoder decoder;
    private PrintWriter printWriter;

    public FlacToCsvConverter() {

    }

    @Override
    public void processMetadata(Metadata metadata) {

    }

    @Override
    public void processFrame(Frame frame) {
        var data = decoder.getChannelData();
        IntStream
                .range(0, frame.header.blockSize)
                .mapToObj(i -> Arrays
                        .stream(data)
                        .filter(Objects::nonNull)
                        .map(ch -> ch.getOutput()[i])
                        .map(String::valueOf)
                        .collect(Collectors.joining(",")))
                .forEach(printWriter::println);
    }

    @Override
    public void processError(String msg) {

    }

    @Override
    public void processStreamInfo(StreamInfo streamInfo) {

    }

    @Override
    public void processPCM(ByteData pcm) {

    }

    public void flacToCsv(String inName, String outName, int skips) {
        try (var inStream = new RandomFileInputStream(inName);
             var printWriter = new PrintWriter(outName)
        ) {
            this.decoder = new FLACDecoder(inStream);
            decoder.addFrameListener(this);
            decoder.addPCMProcessor(this);
            decoder.seek(skips);
            this.printWriter = printWriter;
            decoder.decode();
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error when converting flac to csv.");
        }
        finally {
            this.decoder = null;
            this.printWriter = null;
        }
    }


    public static void convertFlacToCsv(String inName, String outName, int skips) {
        try (var inStream = new RandomFileInputStream(inName);
             var printWriter = new PrintWriter(outName);
        ) {
            var decoder = new FLACDecoder(inStream);
            decoder.readMetadata();
            decoder.seek(skips);
            var shouldContinue = true;
            var skipped = true;
            while (shouldContinue) {
                var frame = decoder.readNextFrame();

                if (!skipped) {
                    decoder.seek(skips);
                    skipped = true;
                }

                if (frame == null && decoder.isEOF()) {
                    shouldContinue = false;
                }
                else if (frame == null && !decoder.isEOF()) {
                    logger.warn("Bad frame found!");
                }
                else {
                    var data = decoder.getChannelData();
                    /*IntStream.range(0, frame != null ? frame.header.blockSize : 0)
                            .mapToObj(i -> Arrays.stream(data)
                                    .filter(Objects::nonNull)
                                    .map(ch -> String.valueOf(ch.getOutput()[i]))
                                    .collect(Collectors.joining(","))
                            )
                            //.forEach((s) -> {});
                            .forEach(printWriter::println);*/
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error when converting flac to csv.");
        }
    }
}





























