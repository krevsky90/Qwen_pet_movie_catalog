package com.krev.qwen_pet_movie_catalog.services;

import com.krev.qwen_pet_movie_catalog.dto.MovieRequest;
import com.krev.qwen_pet_movie_catalog.dto.MovieResponse;
import com.krev.qwen_pet_movie_catalog.entity.Movie;
import com.krev.qwen_pet_movie_catalog.helpers.RedisCacheHelper;
import com.krev.qwen_pet_movie_catalog.mapper.MovieMapper;
import com.krev.qwen_pet_movie_catalog.repo.MovieRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.krev.qwen_pet_movie_catalog.configuration.CacheConstants.*;

@Service
@Transactional(readOnly = true)
public class MovieService {
    private final MovieRepository repository;
    private final MovieMapper movieMapper;  //внедрится автоматически
    private final RedisTemplate<String, Object> redisTemplate;

    public MovieService(MovieRepository repository, MovieMapper movieMapper, RedisTemplate<String, Object> redisTemplate) {
        this.repository = repository;
        this.movieMapper = movieMapper;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public MovieResponse createMovie(MovieRequest requestDto) {
        Movie movieToSave = movieMapper.toEntity(requestDto);
        Movie savedMovie = repository.save(movieToSave);
        MovieResponse result = movieMapper.toDto(savedMovie);

        RedisCacheHelper.cacheEvictByPattern(redisTemplate, MOVIES_LIST);
        RedisCacheHelper.cacheEvictByPattern(redisTemplate, MOVIES_SEARCH);

        return result;
    }

    //NOTE: cache should store DTO (JSON response), but not Entity!
    public Optional<MovieResponse> findMovieById(Long id) {
        String key = RedisCacheHelper.cacheKeyBuild(MOVIE_BY_ID, id);
        //try cache
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof MovieResponse response) {
            return Optional.of(response);
        }

        //cache miss -> go to DB
        Optional<Movie> movieOpt = repository.findById(id);
        // if found -> put to cache
        if (movieOpt.isPresent()) {
            MovieResponse movieResponse = movieMapper.toDto(movieOpt.get());
            RedisCacheHelper.cachePut(redisTemplate, key, movieResponse, TTL_BY_ID);
            return Optional.of(movieResponse);
        }

        return Optional.empty();
    }

    public Page<MovieResponse> findAllMovies(Pageable pageable) {
        //example of key: movie:moviesList::03UNSORTED
        String key = RedisCacheHelper.cacheKeyBuild(MOVIES_LIST,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort().toString());

        //try cache
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof Page) {
            return (Page<MovieResponse>) cached;
        }

        //cache miss -> go to DB
        Page<MovieResponse> result = repository.findAll(pageable).map(movieMapper::toDto);
        //save to cache
        RedisCacheHelper.cachePut(redisTemplate, key, result, TTL_LIST_SEARCH);

        return result;
    }

    public Page<MovieResponse> searchMovies(String title, Pageable pageable) {
        String key = RedisCacheHelper.cacheKeyBuild(MOVIES_SEARCH,
                title,
                pageable.getPageNumber(),
                pageable.getPageSize());

        //try cache
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof Page) {
            return (Page<MovieResponse>) cached;
        }

        //cache miss -> go to DB
        Page<MovieResponse> result = repository.findByTitleContaining(title, pageable).map(movieMapper::toDto);
        //save to cache
        RedisCacheHelper.cachePut(redisTemplate, key, result, TTL_LIST_SEARCH);

        return result;
    }

    // UPDATE: обновляем БД + сбрасываем кэш
    @Transactional
    public Optional<MovieResponse> updateMovie(Long id, MovieRequest requestDto) {
        Optional<MovieResponse> result = repository.findById(id)
                .map(movie -> {
                    movieMapper.updateEntityFromRequest(requestDto, movie);
                    Movie saved = repository.save(movie);
                    return movieMapper.toDto(saved);
                });

        if (result.isPresent()) {
            //invalidate byId/list/search caches
            String key = RedisCacheHelper.cacheKeyBuild(MOVIE_BY_ID, id);
            RedisCacheHelper.cacheEvict(redisTemplate, key);
            RedisCacheHelper.cacheEvictByPattern(redisTemplate, MOVIES_LIST);
            RedisCacheHelper.cacheEvictByPattern(redisTemplate, MOVIES_SEARCH);
        }

        return result;
    }

    @Transactional
    public void deleteMovie(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);

            //invalidate caches
            String key = RedisCacheHelper.cacheKeyBuild(MOVIE_BY_ID, id);
            RedisCacheHelper.cacheEvict(redisTemplate, key);
            RedisCacheHelper.cacheEvictByPattern(redisTemplate, MOVIES_LIST);
            RedisCacheHelper.cacheEvictByPattern(redisTemplate, MOVIES_SEARCH);
        } else {
            throw new EntityNotFoundException("Movie with id " + id + " not found");
        }
    }
}
