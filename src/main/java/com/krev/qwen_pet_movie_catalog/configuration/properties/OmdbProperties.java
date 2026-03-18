package com.krev.qwen_pet_movie_catalog.configuration.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "external.omdb")
@Validated
public record OmdbProperties(
        @NotBlank String apiKey,
        @NotBlank String baseUrl    //not used now, but might be useful later
) {

}
