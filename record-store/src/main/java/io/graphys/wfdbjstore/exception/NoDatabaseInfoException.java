package io.graphys.wfdbjstore.exception;

public class NoDatabaseInfoException extends RuntimeException {
    public NoDatabaseInfoException() {

    }

    public NoDatabaseInfoException(String msg) {
        super(msg);
    }

    public NoDatabaseInfoException(String msg, Throwable e) {
        super(msg, e);
    }
}
