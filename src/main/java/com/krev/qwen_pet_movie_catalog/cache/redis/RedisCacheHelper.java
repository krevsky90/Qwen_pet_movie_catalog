package com.krev.qwen_pet_movie_catalog.cache.redis;

import com.krev.qwen_pet_movie_catalog.cache.CacheOperations;
import org.springframework.stereotype.Component;

import static com.krev.qwen_pet_movie_catalog.configuration.CacheConstants.CACHE_DELIMITER;
import static com.krev.qwen_pet_movie_catalog.configuration.CacheConstants.KEY_PREFIX;


@Component
public class RedisCacheHelper implements CacheOperations {
    @Override
    public String cacheKeyBuild(String cacheName, Object... keyParts) {
        StringBuilder sb = new StringBuilder(KEY_PREFIX).append(cacheName).append(CACHE_DELIMITER);
        for (Object part : keyParts) {
            sb.append(part);
        }
        return sb.toString();
    }
}
