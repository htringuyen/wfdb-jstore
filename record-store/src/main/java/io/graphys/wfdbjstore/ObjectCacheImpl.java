package io.graphys.wfdbjstore;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An implementation of CachePool interface using HashMap
 * @param <K> Type of the key
 * @param <V> Type of the caching target
 */
public class ObjectCacheImpl<K, V> implements ObjectCache<K, V> {
    private final int cacheLimit;
    private final Map<K, V> cacheMap = new HashMap<>();
    private final Queue<K> cacheKeys = new LinkedList<>();
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Limit number of cached objects in the pool.
     * If the limit exceeded, remove the oldest object in the pool.
     * @param limit limit number of cached objects
     */
    ObjectCacheImpl(int limit) {
        this.cacheLimit = limit;
    }

    @Override
    public void put(K key, V value) {
        if (key == null) return;
        lock.lock();
        try {
            if (cacheMap.containsKey(key)) {
                cacheMap.put(key, value);
            }
            else if (cacheMap.size() < cacheLimit) {
                cacheKeys.add(key);
                cacheMap.put(key, value);
            }
            else {
                var removedKey = cacheKeys.poll();
                cacheMap.remove(removedKey);
                cacheMap.put(key, value);
                cacheKeys.add(key);
            }
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public V get(K key) {
        lock.lock();
        try {
            return cacheMap.get(key);
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        try {
            lock.lock();
            cacheMap.clear();
        }
        finally {
            lock.unlock();
        }
    }
}