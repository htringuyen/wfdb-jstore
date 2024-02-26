package io.graphys.wfdbjstore;

/**
 * Caching pool, store an object of type V and its key of type K
 * so that V can be retrieved later (by passing its key) without instantiating.
 * @param <K> Type of the key object
 * @param <V> Type of target object
 */
public interface ObjectCache<K, V> {
    /**
     * Put the target object V into the cache pool so that the object can be retrieved later by its key K.
     * @param key The key
     * @param value The cached object
     * @return true if new K-V pair added to the pool
     */
    void put(K key, V value);

    /**
     * Retrieve cached object by passing its key.
     * @param key The key
     * @return The cached object
     */
    V get(K key);

    /**
     * Clear currently cached objects
     */
    void clear();
}
