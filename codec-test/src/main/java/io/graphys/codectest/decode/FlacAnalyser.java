package io.graphys.codectest.decode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jflac.FLACDecoder;
import org.jflac.FrameListener;
import org.jflac.frame.Frame;
import org.jflac.metadata.Metadata;

import java.io.*;


public class FlacAnalyser implements FrameListener, AutoCloseable {
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

}
