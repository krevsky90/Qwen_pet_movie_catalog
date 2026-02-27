package com.krev.qwen_pet_movie_catalog.services;

import com.krev.qwen_pet_movie_catalog.dto.MovieRequest;
import com.krev.qwen_pet_movie_catalog.dto.MovieResponse;
import com.krev.qwen_pet_movie_catalog.entity.Movie;
import com.krev.qwen_pet_movie_catalog.mapper.MovieMapper;
import com.krev.qwen_pet_movie_catalog.repo.MovieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MovieServiceTest {
    @Mock
    private MovieRepository movieRepository;
    @Mock
    private MovieMapper movieMapper;

    @InjectMocks
    private MovieService movieService;

    @Test
    void shouldCreateMovie_shouldReturnCreatedStatusAndLocationHeader() {
        //when
        MovieRequest request = new MovieRequest("title1", 1995, "Comedy");
        Movie entity = new Movie(null, "title1", 1995, "Comedy");
        Movie savedEntity = new Movie(34L, "title1", 1995, "Comedy");
        MovieResponse expectedResponse = new MovieResponse(34L, "title1", 1995, "Comedy", LocalDateTime.of(2026, 1, 15, 13, 0), LocalDateTime.of(2026, 1, 15, 13, 0));

        when(movieMapper.toEntity(request)).thenReturn(entity);
        when(movieRepository.save(entity)).thenReturn(savedEntity);
        when(movieMapper.toDto(savedEntity)).thenReturn(expectedResponse);

        //act
        MovieResponse actualResponse = movieService.createMovie(request);

        //assert
        assertThat(actualResponse).isEqualTo(expectedResponse);

        verify(movieMapper).toEntity(request);
        verify(movieRepository).save(entity);
        verify(movieMapper).toDto(savedEntity);
    }
}
