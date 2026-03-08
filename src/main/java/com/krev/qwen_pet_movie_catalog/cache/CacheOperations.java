package com.krev.qwen_pet_movie_catalog.cache;

import java.time.Duration;

public interface CacheOperations {
    void cachePut(String key, Object value, Duration ttl);

    void cacheEvict(String key);

    //for list/search caches
    void cacheEvictByPattern(String pattern);

    //Objects... can handle strings and longs
    String cacheKeyBuild(String cacheName, Object... keyParts);
}
