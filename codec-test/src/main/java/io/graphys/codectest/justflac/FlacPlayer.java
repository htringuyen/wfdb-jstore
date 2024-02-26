package io.graphys.codectest.justflac;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

import org.kc7bfi.jflac.FLACDecoder;
import org.kc7bfi.jflac.FrameListener;
import org.kc7bfi.jflac.frame.Frame;
import org.kc7bfi.jflac.io.RandomFileInputStream;
import org.kc7bfi.jflac.metadata.Metadata;


public class FlacPlayer implements FrameListener {
    private static final Logger logger = LogManager.getLogger(FlacPlayer.class);
    private long sumAll;
    private FLACDecoder flacDecoder;
    private final String inName;

    public FlacPlayer(String inName) throws FileNotFoundException {
        if (inName == null) {
            throw new FileNotFoundException("Cannot find input file for FLAC decoder.");
        }
        this.inName = inName;
    }

    public FlacPlayer() throws FileNotFoundException {
        if (!FlacContext.INPUT_NAME.isBound()) {
            throw new FileNotFoundException("Input file for FLAC decoder does not set.");
        }
        this.inName = FlacContext.INPUT_NAME.get();
    }

    @Override
    public void processMetadata(Metadata metadata) {
    }

    @Override
    public void processFrame(Frame frame) {
        var data = flacDecoder.getChannelData();
        sumAll += IntStream
                .range(0, frame.header.blockSize)
                .map(i -> Arrays.stream(data)
                        .filter(Objects::nonNull)
                        .map(ch -> ch.getOutput()[i])
                        .mapToInt(v -> v)
                        .sum()
                )
                .sum();
    }

    @Override
    public void processError(String msg) {

    }

    public Long sumAllSignals() throws IOException {
        sumAll = 0;
        try (var in = getInput()) {
            flacDecoder = new FLACDecoder(in);
            flacDecoder.addFrameListener(this);
            flacDecoder.decode();
            flacDecoder = null;
            return sumAll;
        }
    }

    private InputStream getInput() throws FileNotFoundException{
        return new FileInputStream(inName);
    }

    public static Long sumAllSignalsStepping(int nSteps) {
        try (
                var inStream = new RandomFileInputStream(FlacContext.INPUT_NAME.get());
        ) {
            var decoder = new FLACDecoder(inStream);
            decoder.readMetadata();

            var shouldContinue = true;
            long sumAll = 0;
            long nextPos = 0;

            while (shouldContinue) {
                var frame = decoder.readNextFrame();

                if (frame == null && decoder.isEOF()) {
                    shouldContinue = false;
                }
                else if (frame == null && !decoder.isEOF()) {
                    logger.warn("Bad frame found!");
                }
                else {
                    nextPos = frame.header.sampleNumber + frame.header.blockSize + nSteps;
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
                if (nextPos < decoder.getStreamInfo().getTotalSamples()) {
                    decoder.seek(nextPos);
                }
            }
            return sumAll;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return -1L;
    }
}






















