package com.krev.qwen_pet_movie_catalog.services;

import com.krev.qwen_pet_movie_catalog.dto.MovieRequest;
import com.krev.qwen_pet_movie_catalog.dto.MovieResponse;
import com.krev.qwen_pet_movie_catalog.entity.Movie;
import com.krev.qwen_pet_movie_catalog.external.omdb.OmdbClient;
import com.krev.qwen_pet_movie_catalog.external.omdb.dto.OmdbResponse;
import com.krev.qwen_pet_movie_catalog.mapper.MovieMapper;
import com.krev.qwen_pet_movie_catalog.repo.MovieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MovieServiceTest {
    @Mock
    private MovieRepository movieRepository;
    @Mock
    private MovieMapper movieMapper;
    @Mock
    private OmdbClient omdbClient;

    @InjectMocks
    private MovieService movieService;

    @Test
    void shouldCreateMovie_shouldReturnCreatedStatusAndLocationHeader() {
        //when
        MovieRequest request = new MovieRequest("title1", 1995, "Comedy");
        Movie entity = new Movie(null, "title1", 1995, "Comedy");

        String poster = "https://m.media-amazon.com/images/M/MV5BMjAxMzY3NjcxNF5BMl5BanBnXkFtZTcwNTI5OTM0Mw@@._V1_SX300.jpg";
        String imdbRating = "8.8";
        String plot = "my_plot";
        String director = "Christopher Nolan";

        OmdbResponse omdbResponse = new OmdbResponse(
                "title1",
                "1995",
                "PG-13",
                "16 Jul 2010",
                "148 min",
                null,
                director,
                null,
                plot,
                null,
                null,
                null,
                poster,
                List.of(),
                null,
                imdbRating,
                null,
                null,
                null,
                null,
                null
        );
        Movie savedEntity = new Movie(34L, "title1", 1995, "Comedy");
        MovieResponse expectedResponse = new MovieResponse(34L, "title1", 1995, "Comedy",
                poster, imdbRating, plot, director,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                LocalDateTime.of(2026, 1, 15, 13, 0));

        when(movieMapper.toEntity(request)).thenReturn(entity);
        when(omdbClient.getMovieByTitleAndYear(any(), eq("title1"), eq(1995))).thenReturn(omdbResponse);
        when(movieRepository.save(entity)).thenReturn(savedEntity);
        when(movieMapper.toDto(savedEntity)).thenReturn(expectedResponse);

        //act
        MovieResponse actualResponse = movieService.createMovie(request);

        //assert
        assertThat(actualResponse).isEqualTo(expectedResponse);

        verify(movieMapper).toEntity(request);
        verify(omdbClient).getMovieByTitleAndYear(any(), eq("title1"), eq(1995));
        verify(movieRepository).save(entity);
        verify(movieMapper).toDto(savedEntity);
    }
}
