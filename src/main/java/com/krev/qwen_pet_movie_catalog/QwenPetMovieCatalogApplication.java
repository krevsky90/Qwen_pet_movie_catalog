package com.krev.qwen_pet_movie_catalog;

import com.krev.qwen_pet_movie_catalog.configuration.properties.OmdbProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.krev.qwen_pet_movie_catalog.external")
@SpringBootApplication
@EnableConfigurationProperties(OmdbProperties.class)
public class QwenPetMovieCatalogApplication {

    public static void main(String[] args) {
        SpringApplication.run(QwenPetMovieCatalogApplication.class, args);
    }

}
