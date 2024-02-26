package io.graphys.wfdbjstore.transaction;

import java.util.concurrent.locks.ReentrantLock;
import wfdb.*;

public class NativeTransaction {
    private final ReentrantLock lock = new ReentrantLock();
    private static final NativeTransaction singleton;

    static {
        singleton = new NativeTransaction();
    }

    private NativeTransaction() {

    }

    public static NativeTransaction get() {
        return singleton;
    }

    public<R> R start(Action<R> action) {
        lock.lock();
        try {
            return action.perform();
        }
        finally {
            lock.unlock();
        }
    }

    public static<R> R startTransaction(Action<R> action) {
        return singleton.start(action);
    }

}




































