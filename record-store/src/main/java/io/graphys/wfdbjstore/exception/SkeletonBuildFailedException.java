package io.graphys.wfdbjstore.exception;

public class SkeletonBuildFailedException extends RuntimeException {
    public SkeletonBuildFailedException() {
        super();
    }

    public SkeletonBuildFailedException(String msg) {
        super(msg);
    }

    public SkeletonBuildFailedException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}
