package com.krev.qwen_pet_movie_catalog.services;

import com.krev.qwen_pet_movie_catalog.dto.MovieRequest;
import com.krev.qwen_pet_movie_catalog.dto.MovieResponse;
import com.krev.qwen_pet_movie_catalog.entity.Movie;
import com.krev.qwen_pet_movie_catalog.mapper.MovieMapper;
import com.krev.qwen_pet_movie_catalog.repo.MovieRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MovieService {
    private final MovieRepository repository;
    private final MovieMapper movieMapper;  //внедрится автоматически

    //NOTE: После добавления MapStruct пересобери проект (./gradlew build),
    // иначе IDE может не видеть сгенерированный класс MovieMapperImpl
    public MovieService(MovieRepository repository, MovieMapper movieMapper) {
        this.repository = repository;
        this.movieMapper = movieMapper;
    }

    @Transactional
    public MovieResponse createMovie(MovieRequest requestDto) {
        Movie movieToSave = movieMapper.toEntity(requestDto);
        movieToSave.setCreatedAt(LocalDateTime.now());
        movieToSave.setUpdatedAt(LocalDateTime.now());
        Movie savedMovie = repository.save(movieToSave);

        return movieMapper.toDto(savedMovie);
    }

    public Optional<MovieResponse> findMovieById(Long id) {
        return repository.findById(id).map(movieMapper::toDto);
    }

    public List<MovieResponse> findAllMovies() {
        List<Movie> movies = repository.findAll();
        return movies.stream().map(movieMapper::toDto).collect(Collectors.toUnmodifiableList());
    }

    @Transactional
    public Optional<MovieResponse> updateMovie(Long id, MovieRequest requestDto) {
        return repository.findById(id)
                .map(movie -> {
                    movieMapper.updateEntityFromRequest(requestDto, movie);
                    movie.setUpdatedAt(LocalDateTime.now());
                    Movie saved = repository.save(movie);
                    return movieMapper.toDto(saved);
                });
    }

    @Transactional
    public void deleteMovie(Long id) {
        Optional<Movie> byId = repository.findById(id);
        if (byId.isPresent()) {
            repository.deleteById(id);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found");
        }
    }
}
