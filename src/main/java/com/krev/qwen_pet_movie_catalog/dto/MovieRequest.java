package com.krev.qwen_pet_movie_catalog.dto;

public record MovieRequest(
        String title,
        Integer year,
        String genre
) {}
