package com.krev.qwen_pet_movie_catalog.cache.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

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
                        DefaultTyping.NON_FINAL, // Аналог ObjectMapper.DefaultTyping.NON_FINAL
                        JsonTypeInfo.As.PROPERTY // Указываем, что тип хранится в свойстве
                )
                .disable(MapperFeature.USE_ANNOTATIONS) // Опционально: для скорости
                .build();

        // 3. Передаем в сериализатор (к-ый используем вместо дефолтного JdkSerializationRedisSerializer)
        GenericJacksonJsonRedisSerializer jsonRedisSerializer = new GenericJacksonJsonRedisSerializer(mapper);

        redisTemplate.setValueSerializer(jsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jsonRedisSerializer);  //for 'hash' type in Redis

        //constructs RedisTemplate bean
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
