package com.krev.qwen_pet_movie_catalog.controllers;

import com.krev.qwen_pet_movie_catalog.dto.MovieRequest;
import com.krev.qwen_pet_movie_catalog.dto.MovieResponse;
import com.krev.qwen_pet_movie_catalog.services.MovieService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/movies")
public class MovieController {
    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping
    //todo: add @Valid after @RequestBody
    public ResponseEntity<MovieResponse> createMovie(@RequestBody MovieRequest requestDto,
                                                     UriComponentsBuilder uriBuilder) {
        MovieResponse responseDto = movieService.createMovie(requestDto);

        //NOTE: location возвращается как отдельное поле в headers, это хороший тон (т.к. у отправителя теперь есть ссылка на ресурс)
        URI location = uriBuilder.path("/api/v1/movies/{id}").buildAndExpand(responseDto.id()).toUri();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .location(location)
                .body(responseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        return movieService.findMovieById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<MovieResponse>> getMovies() {
        List<MovieResponse> allMovies = movieService.findAllMovies();
        return ResponseEntity.ok(allMovies);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovieResponse> updateMovie(@PathVariable Long id, @RequestBody MovieRequest requestDto) {
        return movieService.updateMovie(id, requestDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }
}
