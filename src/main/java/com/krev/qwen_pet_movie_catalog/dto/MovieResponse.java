package com.krev.qwen_pet_movie_catalog.dto;

import java.time.LocalDateTime;

public record MovieResponse(
        Long id,
        String title,
        Integer year,
        String genre,
        String poster,
        String imdbRating,
        String plot,
        String director,
//        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")    //jackson
        LocalDateTime createdAt,
//        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")    //jackson
        LocalDateTime updatedAt
) {
}
