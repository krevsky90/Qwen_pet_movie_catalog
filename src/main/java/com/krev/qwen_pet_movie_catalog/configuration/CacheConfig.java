package com.krev.qwen_pet_movie_catalog.configuration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

@Configuration
@EnableCaching  // ← Включаем аннотации @Cacheable, @CacheEvict
public class CacheConfig {
    // используем JSON-сериализацию вместо Java-сериализатора по умолчанию
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        //defaultCacheConfig - uses java-serialization by default
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10L))
                .prefixCacheNameWith("cache:movie:")
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()
                        )
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJacksonJsonRedisSerializer(new ObjectMapper())
                        )
                );

        return RedisCacheManager.builder(factory)
                .cacheDefaults(configuration)
                .build();

    }
}
