package com.krev.qwen_pet_movie_catalog.dto;

import java.time.LocalDateTime;

public record MovieResponse (
        Long id,
        String title,
        Integer year,
        String genre,
//        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")    //jackson
        LocalDateTime createdAt,
//        @JsonFormat(pattern = "yyyy-MM-dd'T"HH:mm:ss")    //jackson
        LocalDateTime updatedAt
) implements java.io.Serializable {
    // 🔥 records автоматически генерируют serialVersionUID,
    // но можно задать явно для контроля совместимости:
    private static final long serialVersionUID = 1L;
}
