package com.krev.qwen_pet_movie_catalog.external.omdb;

import feign.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OmdbClientConfig {
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
