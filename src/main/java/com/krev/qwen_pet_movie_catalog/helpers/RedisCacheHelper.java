package com.krev.qwen_pet_movie_catalog.helpers;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static com.krev.qwen_pet_movie_catalog.configuration.CacheConstants.KEY_PREFIX;

public class RedisCacheHelper {
    private RedisCacheHelper() {
    }

    public static void cachePut(RedisTemplate<String, Object> template, String key, Object value, Duration ttl) {
        template.opsForValue().set(key, value, ttl);
    }

    public static void cacheEvict(RedisTemplate<String, Object> template, String key) {
        template.delete(key);
    }

    //for list/search caches

    /**
     * bad practice! need to use SCAN, i.e. cacheEvictByPattern method
     */
    public static void cacheEvictByPatternDeprecated(RedisTemplate<String, Object> template, String pattern) {
        Set<String> keys = template.keys(KEY_PREFIX + pattern + "*");
        if (keys != null && !keys.isEmpty()) {
            template.delete(keys);
        }
    }

    public static void cacheEvictByPattern(RedisTemplate<String, Object> template, String pattern) {
        Set<String> keys = new HashSet<>();

        // SCAN — неблокирующая альтернатива KEYS
        try (Cursor<String> cursor = template.scan(ScanOptions.scanOptions()
                .match(KEY_PREFIX + pattern + "*")
                .count(100) //hint - to batch reading of keys (by ~ 100). Note! Redis might ignore it!
                .build())) {

            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        }

        if (!keys.isEmpty()) {
            template.delete(keys);
        }
    }

    //Objects... can handle strings and longs
    public static String cacheKeyBuild(String cacheName, Object... keyParts) {
        StringBuilder sb = new StringBuilder(KEY_PREFIX).append(cacheName).append("::");
        for (Object part : keyParts) {
            sb.append(part);
        }
        return sb.toString();
    }
}
