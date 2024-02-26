package io.graphys.wfdbjstore.exception;

public class CacheWriterFailedException extends RuntimeException {
    public CacheWriterFailedException() {
        super();
    }

    public CacheWriterFailedException(String msg) {
        super(msg);
    }

    public CacheWriterFailedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
