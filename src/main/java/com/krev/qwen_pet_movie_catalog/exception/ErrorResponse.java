package com.krev.qwen_pet_movie_catalog.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        Map<String, String> errors, // ← здесь будут "year": "Year too old"
        String message) {
}
