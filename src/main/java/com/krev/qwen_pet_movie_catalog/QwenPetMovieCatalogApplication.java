package com.krev.qwen_pet_movie_catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.krev.qwen_pet_movie_catalog.external")
@SpringBootApplication
public class QwenPetMovieCatalogApplication {

    public static void main(String[] args) {
        SpringApplication.run(QwenPetMovieCatalogApplication.class, args);
    }

}
