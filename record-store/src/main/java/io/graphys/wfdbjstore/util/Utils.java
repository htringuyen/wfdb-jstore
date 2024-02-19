package io.graphys.wfdbjstore.util;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Gather utility functions
 */
public class Utils {

    private Utils() {}

    /**
     * Check if the given uri points to an existing file
     * @param uri uri of either file or http scheme
     * @return true if file exists in local file system or http request is valid
     */
    public static boolean fileExists(URI uri) {
        // if this is http or https uri then perform head request to check if file exists
        if (uri.getScheme() != null
                && (uri.getScheme().equals("http")
                || uri.getScheme().equals("https"))) {
            try {
                var conn = (HttpURLConnection) uri.toURL().openConnection();
                conn.setRequestMethod("HEAD");
                return conn.getResponseCode() == 200;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (uri.getScheme() != null && uri.getScheme().equals("file")) {
            var file = new File(uri);
            return file.exists();
        }
        else {
            throw new RuntimeException("Unsupported uri scheme");
        }

        return false;
    }

    static URI uriAppend(URI uri, String fragment) {
        return uri.resolve(fragment);
    }


    static Map<String, List<String>> headForHeaders(URL url) {
        try  {
            var conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            var headerMap = conn.getHeaderFields();
            conn.disconnect();
            return headerMap;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
