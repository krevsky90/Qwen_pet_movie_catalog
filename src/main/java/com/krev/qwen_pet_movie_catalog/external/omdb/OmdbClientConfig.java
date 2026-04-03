package com.krev.qwen_pet_movie_catalog.external.omdb;

import com.krev.qwen_pet_movie_catalog.configuration.properties.OmdbProperties;
import feign.Logger;
import feign.Request;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OmdbProperties.class)
public class OmdbClientConfig {
    // Таймауты на уровне Feign
    @Bean
    public Request.Options feignOptions() {
        return new Request.Options(
                3000,  // connect timeout: 3 секунды
                5000,  // read timeout: 5 секунд
                true   // follow redirects
        );
    }

    //это логгирование для самого feign API, к-ое получает JSON-ответ
    // 🔥 Уровень 2: Feign Logger — генерирует логи запросов/ответов
    @Bean
    public feign.Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

//     🔥 2. Сам логгер (расширяем абстрактный класс feign.Logger)
    @Bean
    public Logger feignLogger() {
        // Создаём SLF4J-логгер для вывода
        org.slf4j.Logger slf4jLogger = LoggerFactory.getLogger(OmdbClient.class);

        // Возвращаем реализацию feign.Logger
        return new Logger() {
            @Override
            protected void log(String configKey, String format, Object... args) {
                // 🔥 Преобразуем %s → {} для совместимости со SLF4J
                String slf4jFormat = format.replace("%s", "{}").replace("%d", "{}");

                // Используем SLF4J-форматирование
                var formattingResult = MessageFormatter.arrayFormat(slf4jFormat, args);
                slf4jLogger.debug(formattingResult.getMessage());
            }
        };
    }
}
