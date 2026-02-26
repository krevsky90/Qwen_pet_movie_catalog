package com.krev.qwen_pet_movie_catalog.repo;

import com.krev.qwen_pet_movie_catalog.entity.Movie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest        // 1. Загружает только JPA компоненты, instead of SpringBootTest!
@Testcontainers     // 2. Управляет жизненным циклом контейнеров
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)    // 3. ЗАПРЕЩАЕТ замену на H2
public class MovieRepositoryTest {
    // PostgreSQL контейнер
    @Container
    static PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("movie_test_db")
            .withUsername("test")
            .withPassword("test");

    // Динамическое свойство для подключения к БД
    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private MovieRepository repository;

    @Test
    @DisplayName("Save movie and find it by title")
    void shouldCreateMovie() {
        //Given
        String title = "movieTitle";
        int year = 1990;
        String genre = "Thriller";

        Movie newMovie = new Movie();
        newMovie.setTitle(title);
        newMovie.setYear(year);
        newMovie.setGenre(genre);

        //When
        Movie savedMovie = repository.save(newMovie);
        Optional<Movie> foundMovieOpt = repository.findByTitle(title);

        //Then
        assertThat(foundMovieOpt).isPresent().hasValueSatisfying(movie -> {
            assertThat(movie.getTitle()).isEqualTo(title);
            assertThat(movie.getYear()).isEqualTo(year);
            assertThat(movie.getGenre()).isEqualTo(genre);
        });
        assertThat(savedMovie.getId()).isNotNull();
    }
}