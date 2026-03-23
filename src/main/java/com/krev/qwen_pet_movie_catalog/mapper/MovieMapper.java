package com.krev.qwen_pet_movie_catalog.mapper;

import com.krev.qwen_pet_movie_catalog.dto.MovieRequest;
import com.krev.qwen_pet_movie_catalog.dto.MovieResponse;
import com.krev.qwen_pet_movie_catalog.entity.Movie;
import org.mapstruct.*;

@Mapper(componentModel = "spring",  //важно для Spring DI
        unmappedTargetPolicy = ReportingPolicy.ERROR    //Если ты забудешь поле, то компиляция упадет (Unmapped target property...)
)

public interface MovieMapper {
    //явно игнорим те поля, к-ые не приходят из resuestDto в Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "poster", ignore = true)
    @Mapping(target = "imdbRating", ignore = true)
    @Mapping(target = "plot", ignore = true)
    @Mapping(target = "director", ignore = true)
    Movie toEntity(MovieRequest requestDto);

    @Mapping(target = "displayTitle",
            expression = "java(buildDisplayTitle(entity.getTitle(), entity.getYear()))")
    @Mapping(target = "ratingCategory", source = "imdbRating", qualifiedByName = "ratingCategory")
    MovieResponse toDto(Movie entity);


    //явно игнорим те поля, к-ые не приходят из resuestDto в Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "poster", ignore = true)
    @Mapping(target = "imdbRating", ignore = true)
    @Mapping(target = "plot", ignore = true)
    @Mapping(target = "director", ignore = true)
    // Если нужно обновлять существующую сущность:
    void updateEntityFromRequest(MovieRequest requestDto, @MappingTarget Movie entity);

    //custom methods
    default String buildDisplayTitle(String title, Integer year) {
        if (title == null) return null;
        if (year == null) {
            return title;
        } else {
            return title + " (" + year + ")";
        }
    }

    // т.к. Любой метод вида A → B воспринимается как глобальный конвертер
    // то необх указывать имя.
    // ИЛИ четкий маппинг полей:
    //    @BeanMapping(ignoreByDefault = true)
    //    @Mapping(target = "title", source = "title")
    @Named("ratingCategory")
    default String mapRatingCategory(String imdbRating) {
        if (imdbRating == null) return "UNKNOWN";
        try {
            double rating = Double.parseDouble(imdbRating);
            if (rating >= 8.0) return "HIGH";
            if (rating >= 5.0) return "MEDIUM";
            return "LOW";
        } catch (NumberFormatException ex) {
            return "UNKNOWN";
        }

    }
}