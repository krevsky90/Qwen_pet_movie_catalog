package com.krev.qwen_pet_movie_catalog.configuration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching  // ← Включаем аннотации @Cacheable, @CacheEvict
public class CacheConfig {
}
