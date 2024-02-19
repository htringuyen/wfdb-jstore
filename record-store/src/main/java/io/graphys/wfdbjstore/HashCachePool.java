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
public class HashCachePool<K, V> implements CachePool<K, V> {
    private K lastCachedId;
    private int cachingLimit;
    private final Map<K, V> cacheMap = new HashMap<>();
    private final Queue<K> cachingQueue = new LinkedList<>();
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Limit number of cached objects in the pool.
     * If the limit exceeded, remove the oldest object in the pool.
     * @param limit limit number of cached objects
     */
    HashCachePool(int limit) {
        this.cachingLimit = limit;
    }

    @Override
    public boolean put(K key, V value) {
        try {
            lock.lock();

            if (cachingLimit == 0) {
                return false;
            }

            if (key == null || value == null) {
                throw new RuntimeException("Key and value caching must not be null");
            }

            if (cacheMap.size() < cachingLimit) {
                cacheMap.put(key, value);
                cachingQueue.add(key);
                return true;
            }
            else if (cacheMap.containsKey(key)) {
                return false;
            }
            else {
                var removeKey = cachingQueue.poll();
                cacheMap.remove(removeKey);
                cacheMap.put(key, value);
                cachingQueue.add(key);
                return true;
            }
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public V get(K key) {
        try {
            lock.lock();
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
            lastCachedId = null;
        }
        finally {
            lock.unlock();
        }
    }
}