package io.graphys.wfdbjstore.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public interface CachedInputHandler {
    URI getSource();

    File getCachedFile();

    void awaitRequestCache(boolean refresh);

    void requestCache(boolean refresh);

    InputStream getInput() throws IOException;
}
