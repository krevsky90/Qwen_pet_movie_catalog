package com.krev.qwen_pet_movie_catalog.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MovieRequest(
        //in case of constraint violation it throws MethodArgumentNotValidException
        @NotBlank(message = "Title is required")
        @Size(max = 255) String title,

        @Min(value = 1800, message = "Year too old")
        @Max(value = 2100, message = "Year from future")
        Integer year,

        String genre
) {}
