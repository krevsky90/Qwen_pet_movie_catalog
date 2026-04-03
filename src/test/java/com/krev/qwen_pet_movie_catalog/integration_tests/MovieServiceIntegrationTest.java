package com.krev.qwen_pet_movie_catalog.integration_tests;

import com.krev.qwen_pet_movie_catalog.cache.CacheOperations;
import com.krev.qwen_pet_movie_catalog.dto.MovieRequest;
import com.krev.qwen_pet_movie_catalog.dto.MovieResponse;
import com.krev.qwen_pet_movie_catalog.entity.Movie;
import com.krev.qwen_pet_movie_catalog.repo.MovieRepository;
import com.krev.qwen_pet_movie_catalog.services.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.krev.qwen_pet_movie_catalog.configuration.CacheConstants.MOVIE_BY_ID;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Transactional
//@org.springframework.test.context.ActiveProfiles("test")
public class MovieServiceIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("movie_test_db")
                    .withUsername("test")
                    .withPassword("test");

    @Container
    @ServiceConnection
    static GenericContainer<?> redisContainer =
            new GenericContainer<>("redis:7-alpine")
                    .withExposedPorts(6379);
//                    .withReuse(true);   //if necessary for several tests

    @Autowired
    private MovieService movieService;
    @Autowired
    private MovieRepository repository;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private CacheOperations cacheOperations;

    @BeforeEach
    void clearCache() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

    //todo: написать (и дописать) тесты
    //ключ слово ТЕСТ 1: Первый вызов → cache miss → БД → результат сохраняется в кэш
    //из чата https://chat.qwen.ai/c/314d58c3-a4bf-46df-bc5c-f2746e96371b
    @Test
    void findMovieById_shouldCacheResult() {
        //given
        Movie movie = new Movie(null, "MyTestFilm", 2024, "Parody");
        Movie savedMovie = repository.save(movie);

        //when
        MovieResponse movieById = movieService.findMovieById(savedMovie.getId()).orElseThrow();

        //then
        String key = cacheOperations.cacheKeyBuild(MOVIE_BY_ID, savedMovie.getId());
        assertThat(redisTemplate.hasKey(key)).isTrue();
    }

    @Test
    void updateMovie_shouldInvalidateCache() {
        //given
        Movie movie = new Movie(null, "MyTestFilm", 2024, "Parody");
        Movie savedMovie = repository.save(movie);
        Long id = savedMovie.getId();
        String key = cacheOperations.cacheKeyBuild(MOVIE_BY_ID, id);

        //heat cache
        movieService.findMovieById(id);
        assertThat(redisTemplate.hasKey(key)).isTrue();

        //when
        MovieRequest updated = new MovieRequest("MyTestFilm", 2024, "Parody");
        movieService.updateMovie(id, updated);

        //then - check the cache is invalidated
        assertThat(redisTemplate.hasKey(key)).isFalse();
        //NOTE: we can also check if MOVIES_LIST and MOVIES_SEARCH caches are also invalidated
    }
}
