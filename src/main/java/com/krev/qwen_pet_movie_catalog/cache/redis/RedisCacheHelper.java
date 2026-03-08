package com.krev.qwen_pet_movie_catalog.cache.redis;

import com.krev.qwen_pet_movie_catalog.cache.CacheOperations;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static com.krev.qwen_pet_movie_catalog.configuration.CacheConstants.KEY_PREFIX;

@Component
public class RedisCacheHelper implements CacheOperations {
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCacheHelper(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void cachePut(String key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    @Override
    public void cacheEvict(String key) {
        redisTemplate.delete(key);
    }

    /**
     * for list/search caches
     * bad practice! need to use SCAN, i.e. cacheEvictByPattern method
     */
    public void cacheEvictByPatternDeprecated(String pattern) {
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + pattern + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Override
    public void cacheEvictByPattern(String pattern) {
        Set<String> keys = new HashSet<>();

        // SCAN — неблокирующая альтернатива KEYS
        try (Cursor<String> cursor = redisTemplate.scan(ScanOptions.scanOptions()
                .match(KEY_PREFIX + pattern + "*")
                .count(100) //hint - to batch reading of keys (by ~ 100). Note! Redis might ignore it!
                .build())) {

            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        }

        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Override
    public String cacheKeyBuild(String cacheName, Object... keyParts) {
        StringBuilder sb = new StringBuilder(KEY_PREFIX).append(cacheName).append("::");
        for (Object part : keyParts) {
            sb.append(part);
        }
        return sb.toString();
    }
}
