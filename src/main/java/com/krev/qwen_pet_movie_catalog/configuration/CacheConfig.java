package com.krev.qwen_pet_movie_catalog.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
//import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Map;

import static com.krev.qwen_pet_movie_catalog.configuration.CacheConstants.*;

@Configuration
@EnableCaching  // ← Включаем аннотации @Cacheable, @CacheEvict
public class CacheConfig {
    @Value("${app.cache.ttl.movies-default:10m}")
    private Duration ttlMovie;
    @Value("${app.cache.global_prefix:movie:}")
    private String cacheGlobalPrefix;

    // TTL для конкретных кэшей
    @Value("${app.cache.ttl.movies-by-id:10m}")
    private Duration ttlMovieById;
    @Value("${app.cache.ttl.movies-list:5m}")
    private Duration ttlMoviesList;
    @Value("${app.cache.ttl.movies-search:5m}")
    private Duration ttlMoviesSearch;

    // Внедряем автоконфигурированный ObjectMapper от Spring Boot
    private final ObjectMapper objectMapper;

    public CacheConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // используем JSON-сериализацию вместо Java-сериализатора по умолчанию
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        //RedisCacheConfiguration.defaultCacheConfig() - uses java-serialization by default
        RedisCacheConfiguration baseConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttlMovie) //default value for ALL caches
                .computePrefixWith(cacheName -> cacheGlobalPrefix + cacheName + CACHE_DELIMITER) //global prefix for all caches
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()
                        )
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer(objectMapper)
                        )
                );

        //NOTE: for each cache we customize its configuration by inheritance of baseConfig:
//        baseConfiguration.blabla(..).doSmth(..)

        return RedisCacheManager.builder(factory)
                .cacheDefaults(baseConfiguration)
                .withInitialCacheConfigurations(Map.of(
                        MOVIE_BY_ID, baseConfiguration.entryTtl(ttlMovieById),
                        MOVIES_LIST, baseConfiguration.entryTtl(ttlMoviesList),
                        MOVIES_SEARCH, baseConfiguration.entryTtl(ttlMoviesSearch)
                ))
                .build();

    }
}
