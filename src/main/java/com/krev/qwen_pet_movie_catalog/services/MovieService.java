package com.krev.qwen_pet_movie_catalog.services;

import com.krev.qwen_pet_movie_catalog.dto.MovieRequest;
import com.krev.qwen_pet_movie_catalog.dto.MovieResponse;
import com.krev.qwen_pet_movie_catalog.entity.Movie;
import com.krev.qwen_pet_movie_catalog.mapper.MovieMapper;
import com.krev.qwen_pet_movie_catalog.repo.MovieRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

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
        Movie savedMovie = repository.save(movieToSave);

        return movieMapper.toDto(savedMovie);
    }

    public Optional<MovieResponse> findMovieById(Long id) {
        return repository.findById(id).map(movieMapper::toDto);
    }

    public Page<MovieResponse> findAllMovies(Pageable pageable) {
        Page<Movie> movies = repository.findAll(pageable);
        return movies.map(movieMapper::toDto);
    }

    public Page<MovieResponse> searchMovies(String title, Pageable pageable) {
        Page<Movie> movies = repository.findByTitleContaining(title, pageable);
        return movies.map(movieMapper::toDto);
    }

    @Transactional
    public Optional<MovieResponse> updateMovie(Long id, MovieRequest requestDto) {
        return repository.findById(id)
                .map(movie -> {
                    movieMapper.updateEntityFromRequest(requestDto, movie);
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
