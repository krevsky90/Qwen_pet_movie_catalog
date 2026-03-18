package com.krev.qwen_pet_movie_catalog.services;

import com.krev.qwen_pet_movie_catalog.dto.MovieRequest;
import com.krev.qwen_pet_movie_catalog.dto.MovieResponse;
import com.krev.qwen_pet_movie_catalog.entity.Movie;
import com.krev.qwen_pet_movie_catalog.external.omdb.OmdbClient;
import com.krev.qwen_pet_movie_catalog.external.omdb.dto.OmdbResponse;
import com.krev.qwen_pet_movie_catalog.mapper.MovieMapper;
import com.krev.qwen_pet_movie_catalog.repo.MovieRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.krev.qwen_pet_movie_catalog.configuration.CacheConstants.*;

@Service
public class MovieService {
    public static final String TRUE = "True";
    private final static Logger LOGGER = LoggerFactory.getLogger(MovieService.class);

    private final MovieRepository repository;
    private final MovieMapper movieMapper;  //внедрится автоматически
    private final OmdbClient omdbClient;

    //NOTE: После добавления MapStruct пересобери проект (./gradlew build),
    // иначе IDE может не видеть сгенерированный класс MovieMapperImpl
    public MovieService(MovieRepository repository, MovieMapper movieMapper, OmdbClient omdbClient) {
        this.repository = repository;
        this.movieMapper = movieMapper;
        this.omdbClient = omdbClient;
    }

    @Transactional
    @CacheEvict(value = {MOVIES_LIST, MOVIES_SEARCH}, allEntries = true)
    //NOTE: очищать все листовые кеши правильно, НО дорого, если создаем фильмы часто
    //альтернатива - очищать кеш программно и более точечно
    public MovieResponse createMovie(MovieRequest requestDto) {
        Movie movieToSave = movieMapper.toEntity(requestDto);
        enrichMovieFromExternalApi(movieToSave, movieToSave.getTitle(), movieToSave.getYear());
        Movie savedMovie = repository.save(movieToSave);

        return movieMapper.toDto(savedMovie);
    }

    @Cacheable(value = MOVIE_BY_ID, key = "#id", unless = "#result == null")
    public Optional<MovieResponse> findMovieById(Long id) {
        return repository.findById(id).map(movieMapper::toDto);
    }

    @Cacheable(value = MOVIES_LIST, key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + (#pageable.sort.toString() ?: '')")
    public Page<MovieResponse> findAllMovies(Pageable pageable) {
        Page<Movie> movies = repository.findAll(pageable);
        return movies.map(movieMapper::toDto);
    }

    @Cacheable(value = MOVIES_SEARCH, key = "#title + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<MovieResponse> searchMovies(String title, Pageable pageable) {
        Page<Movie> movies = repository.findByTitleContaining(title, pageable);
        return movies.map(movieMapper::toDto);
    }

    // UPDATE: обновляем БД + сбрасываем кэш
    @Transactional   //NOTE: transactional should be BEFORE @Caching!
    @Caching(evict = {
            @CacheEvict(value = MOVIE_BY_ID, key = "#id"),   //invalidate cache for particular ID
            @CacheEvict(value = {MOVIES_LIST, MOVIES_SEARCH}, allEntries = true)
    })
    public Optional<MovieResponse> updateMovie(Long id, MovieRequest requestDto) {
        return repository.findById(id)
                .map(movie -> {
                    movieMapper.updateEntityFromRequest(requestDto, movie);
                    Movie saved = repository.save(movie);
                    return movieMapper.toDto(saved);
                });
    }

    @Transactional  //NOTE: transactional should be BEFORE @Caching!
    @Caching(evict = {
            @CacheEvict(value = MOVIE_BY_ID, key = "#id"),   //invalidate cache for particular ID
            @CacheEvict(value = {MOVIES_LIST, MOVIES_SEARCH}, allEntries = true)
    })
    public void deleteMovie(Long id) {
        Optional<Movie> byId = repository.findById(id);
        if (byId.isPresent()) {
            repository.deleteById(id);
        } else {
            throw new EntityNotFoundException("Movie with id " + id + " not found");
        }
    }

    private void enrichMovieFromExternalApi(Movie movie, String title, Integer year) {
        try {
            OmdbResponse externalData = omdbClient.getMovieByTitleAndYear("9f037f28", title, year);

            if (TRUE.equalsIgnoreCase(externalData.response())) {
                movie.setPoster(externalData.poster());
                movie.setImdbRating(externalData.imdbRating());
                movie.setPlot(externalData.plot());
                movie.setDirector(externalData.director());
            }
        } catch (Exception ex) {
            LOGGER.warn("Failed to enrich movie '{} ({})' from omdb: {}", title, year, ex.getMessage());
            LOGGER.warn("", ex);
            // Не прерываем создание фильма — просто без обогащения
        }
    }
}
