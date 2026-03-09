package com.krev.qwen_pet_movie_catalog.cache.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    /**
     * use GenericJacksonJsonRedisSerializer with extra validation - if the same redisTemplate works with different types of objects
     * OR
     * use JacksonJsonRedisSerializer if you work with particular type. Like
     *      public RedisTemplate<String, MovieResponse> redisTemplate(RedisConnectionFactory factory) {
     *          var template = new RedisTemplate<String, MovieResponse>();
     *          template.setConnectionFactory(factory);
     *          var serializer = new JacksonJsonRedisSerializer<>(MovieResponse.class);
     *          template.setValueSerializer(serializer);
     *          template.setKeySerializer(new StringRedisSerializer());
     *          return template;
     *      }
     *
     */

    /**
     * ATTENTION: we use this bean for tests. since Caching is implemented by @Cacheable annotation !!
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);  //for 'hash' type in Redis

        // 1. Создаем валидатор (БЕЗОПАСНОСТЬ)
        // В Jackson 3 класс называется BasicPolymorphicTypeValidator
        PolymorphicTypeValidator validator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.krev.qwen_pet_movie_catalog") // Разрешаем только ваши пакеты
                .build();

        // 2. Собираем JsonMapper через builder
        JsonMapper mapper = JsonMapper.builder()
                .activateDefaultTyping(
                        validator,
                        ObjectMapper.DefaultTyping.NON_FINAL, // Аналог ObjectMapper.DefaultTyping.NON_FINAL
                        JsonTypeInfo.As.PROPERTY // Указываем, что тип хранится в свойстве
                )
                .disable(MapperFeature.USE_ANNOTATIONS) // Опционально: для скорости
                .build();

        // 3. Передаем в сериализатор (к-ый используем вместо дефолтного JdkSerializationRedisSerializer)
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer(mapper);

        redisTemplate.setValueSerializer(jsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jsonRedisSerializer);  //for 'hash' type in Redis

        //constructs RedisTemplate bean
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
