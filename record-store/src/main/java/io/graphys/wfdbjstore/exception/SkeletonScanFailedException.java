package io.graphys.wfdbjstore.exception;

public class SkeletonScanFailedException extends RuntimeException {
    public SkeletonScanFailedException() {
        super();
    }

    public SkeletonScanFailedException(String msg) {
        super(msg);
    }

    public SkeletonScanFailedException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}
