package com.krev.qwen_pet_movie_catalog.external.omdb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)  // ← Игнорируем лишние поля (DVD, BoxOffice, etc.)
public record OmdbResponse(

        @JsonProperty("Title") String title,
        @JsonProperty("Year") String year,
        @JsonProperty("Rated") String rated,
        @JsonProperty("Released") String released,
        @JsonProperty("Runtime") String runtime,
        @JsonProperty("Genre") String genre,
        @JsonProperty("Director") String director,
        @JsonProperty("Actors") String actors,
        @JsonProperty("Plot") String plot,
        @JsonProperty("Language") String language,
        @JsonProperty("Country") String country,
        @JsonProperty("Awards") String awards,
        @JsonProperty("Poster") String poster,

        // Ratings — массив объектов!
        @JsonProperty("Ratings") List<Rating> ratings,
//        @JsonProperty("Ratings") String ratings,

        @JsonProperty("Metascore") String metascore,
        @JsonProperty("imdbRating") String imdbRating,
        @JsonProperty("imdbVotes") String imdbVotes,
        @JsonProperty("imdbID") String imdbID,
        @JsonProperty("Type") String type,
        @JsonProperty("Response") String response,
        @JsonProperty("Error") String error
) {
    // Вложенный record для элементов рейтинга
    public record Rating(
            @JsonProperty("Source") String source,
            @JsonProperty("Value") String value
    ) {
    }
}