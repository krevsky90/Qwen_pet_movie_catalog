package com.krev.qwen_pet_movie_catalog.mapper;

import com.krev.qwen_pet_movie_catalog.dto.MovieRequest;
import com.krev.qwen_pet_movie_catalog.dto.MovieResponse;
import com.krev.qwen_pet_movie_catalog.entity.Movie;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")  //важно для Spring DI
public interface MovieMapper {
    Movie toEntity(MovieRequest requestDto);

    MovieResponse toDto(Movie entity);

    // Если нужно обновлять существующую сущность:
    void updateEntityFromRequest(MovieRequest requestDto, @MappingTarget Movie entity);
}