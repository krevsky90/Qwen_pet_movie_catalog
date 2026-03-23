package com.krev.qwen_pet_movie_catalog.dto;

import java.time.LocalDateTime;

public record MovieResponse(
        Long id,
        String displayTitle,    // - calculable field. "Название (Год)"
        Integer year,
        String genre,
        String poster,
        String imdbRating,
        String ratingCategory,  // - calculable field. HIGH / MEDIUM / LOW - based on imdbRating
        String plot,
        String director,
//        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")    //jackson
        LocalDateTime createdAt,
//        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")    //jackson
        LocalDateTime updatedAt
) {
}
