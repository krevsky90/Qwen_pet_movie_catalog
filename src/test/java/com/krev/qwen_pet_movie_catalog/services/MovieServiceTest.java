package com.krev.qwen_pet_movie_catalog.services;

import com.krev.qwen_pet_movie_catalog.dto.MovieRequest;
import com.krev.qwen_pet_movie_catalog.dto.MovieResponse;
import com.krev.qwen_pet_movie_catalog.entity.Movie;
import com.krev.qwen_pet_movie_catalog.external.omdb.OmdbGateway;
import com.krev.qwen_pet_movie_catalog.external.omdb.dto.OmdbResponse;
import com.krev.qwen_pet_movie_catalog.mapper.MovieMapper;
import com.krev.qwen_pet_movie_catalog.repo.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MovieServiceTest {
    @Mock
    private MovieRepository movieRepository;
    @Mock
    private MovieMapper movieMapper;
    @Mock
    private OmdbGateway omdbGateway;

    @InjectMocks
    private MovieService movieService;

    private static final String poster = "https://m.media-amazon.com/images/M/MV5BMjAxMzY3NjcxNF5BMl5BanBnXkFtZTcwNTI5OTM0Mw@@._V1_SX300.jpg";
    private static final String imdbRating = "8.8";
    private static final String plot = "my_plot";
    private static final String director = "Christopher Nolan";

    private Movie movie;
    private OmdbResponse omdbResponse;

    @Captor
    private ArgumentCaptor<Movie> movieArgumentCaptor;

    @BeforeEach
    void init() {
        movie = new Movie(34L, "title1", 1995, "Comedy");
        movie.setPoster(poster);
        movie.setImdbRating(imdbRating);
        movie.setPlot(plot);
        movie.setDirector(director);

        omdbResponse = new OmdbResponse(
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
                "True", // для обогащения Movie в случае удачного вызова omdbApi
                null
        );
    }

    @Test
    void createMovie_shouldEnrichFromExternalApiAndReturnDto() {
        //given
        MovieRequest request = new MovieRequest("title1", 1995, "Comedy");
        Movie entity = new Movie(null, "title1", 1995, "Comedy");
//        Movie savedEntity = new Movie(34L, "title1", 1995, "Comedy");
        MovieResponse expectedResponse = new MovieResponse(34L, "title1", 1995, "Comedy",
                poster, imdbRating, plot, director,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                LocalDateTime.of(2026, 1, 15, 13, 0));

        when(movieMapper.toEntity(request)).thenReturn(entity);
        when(omdbGateway.getMovieByTitleAndYear("title1", 1995)).thenReturn(omdbResponse);
//        when(movieRepository.save(any(Movie.class))).thenReturn(savedEntity);
        // Устойчивый мок: возвращает тот же объект, который передали в save()
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(movieMapper.toDto(any(Movie.class))).thenReturn(expectedResponse);

        //when
        MovieResponse actualResponse = movieService.createMovie(request);

        //then
        assertThat(actualResponse).isEqualTo(expectedResponse);

        verify(movieMapper).toEntity(request);
        verify(omdbGateway).getMovieByTitleAndYear("title1", 1995);
        verify(movieRepository).save(any(Movie.class));
        verify(movieMapper).toDto(any(Movie.class));

        // проверяем состояние Entity перед сохранением,
        // т.к. если omdb enrichment все-таки произошел,
        // то сохраняемый объект Movie будет иметь не null-евые поля poster, plot и пр
        // поэтому мы проверяем поля сохраняемого объекта savedMovie, предварительно захватив его
        verify(movieRepository).save(movieArgumentCaptor.capture());
        Movie savedMovie = movieArgumentCaptor.getValue();
        assertThat(savedMovie.getPoster()).isEqualTo(poster);
        assertThat(savedMovie.getImdbRating()).isEqualTo(imdbRating);
        assertThat(savedMovie.getPlot()).isEqualTo(plot);
        assertThat(savedMovie.getDirector()).isEqualTo(director);
    }

    @Test
    void createMovie_shouldHandleExternalApiError_Gracefully() {
        MovieRequest request = new MovieRequest("title1", 1995, "Comedy");
        Movie entity = new Movie(null, "title1", 1995, "Comedy");
        // fallback OmdbResponse, как вернул бы реальный Resilience4j при ошибке
        OmdbResponse fallbackResponse = new OmdbResponse(
                "title1",
                "1995",
                "N/A",
                "N/A",
                "N/A",
                "N/A",
                "N/A",
                "N/A",
                "N/A",
                "N/A",
                "N/A",
                "N/A",
                "N/A",
                List.of(),
                "N/A",
                "N/A",
                "N/A",
                "N/A",
                "N/A",
                "False",
                null
        );

//        Movie savedEntity = new Movie(34L, "title1", 1995, "Comedy");
        MovieResponse expectedResponseWithoutEnrichment = new MovieResponse(34L, "title1", 1995, "Comedy",
                null, null, null, null,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                LocalDateTime.of(2026, 1, 15, 13, 0));

        when(movieMapper.toEntity(request)).thenReturn(entity);
        when(omdbGateway.getMovieByTitleAndYear("title1", 1995)).thenReturn(fallbackResponse);

//        when(movieRepository.save(entity)).thenReturn(savedEntity);
        // Устойчивый мок: возвращает тот же объект, который передали в save()
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(movieMapper.toDto(any(Movie.class))).thenReturn(expectedResponseWithoutEnrichment);

        //when
        MovieResponse actualResponse = movieService.createMovie(request);

        //then 1) DTO возвращён, сервис не упал
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.title()).isEqualTo("title1");
        assertThat(actualResponse.year()).isEqualTo(1995);

        // then 2) Поля обогащения в DTO = null
        assertThat(actualResponse.poster()).isNull();
        assertThat(actualResponse.imdbRating()).isNull();
        assertThat(actualResponse.plot()).isNull();
        assertThat(actualResponse.director()).isNull();

        // then 3) Верификация взаимодействий
        verify(movieMapper).toEntity(request);
        verify(omdbGateway).getMovieByTitleAndYear("title1", 1995);
        verify(movieRepository).save(any(Movie.class));
        verify(movieMapper).toDto(any(Movie.class));

        // then 4) проверяем состояние Entity перед сохранением,
        // что omdb enrichment все-таки произошел,
        // тогда сохраняемый объект Movie будет иметь не null-евые поля poster, plot и пр
        // поэтому мы проверяем поля сохраняемого объекта savedMovie, предварительно захватив его
        verify(movieRepository).save(movieArgumentCaptor.capture());
        Movie savedMovie = movieArgumentCaptor.getValue();

        assertThat(savedMovie.getId()).isNull();           // ещё не сохранён
        assertThat(savedMovie.getTitle()).isEqualTo("title1");
        assertThat(savedMovie.getYear()).isEqualTo(1995);
        assertThat(savedMovie.getGenre()).isEqualTo("Comedy");

        // Поля обогащения должны быть null
        assertThat(savedMovie.getPoster()).isNull();
        assertThat(savedMovie.getImdbRating()).isNull();
        assertThat(savedMovie.getPlot()).isNull();
        assertThat(savedMovie.getDirector()).isNull();
    }

    @Test
    void findMovieById_shouldReturnMovie_WhenExists() {
        //given
        Long movieId = movie.getId();
        MovieResponse expectedResponse = new MovieResponse(34L, "title1", 1995, "Comedy",
                poster, imdbRating, plot, director,
                LocalDateTime.of(2026, 1, 15, 13, 0),
                LocalDateTime.of(2026, 1, 15, 13, 0));

        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
        when(movieMapper.toDto(movie)).thenReturn(expectedResponse);

        //when
        Optional<MovieResponse> actualResponse = movieService.findMovieById(movieId);

        //then
        assertThat(actualResponse).isPresent()
                .hasValueSatisfying(response -> {
                            assertThat(response.id()).isEqualTo(movie.getId());
                            assertThat(response.title()).isEqualTo(movie.getTitle());
                        }
                );

        verify(movieRepository).findById(movie.getId());
        verify(movieMapper).toDto(movie);
    }

    @Test
    void findMovieById_shouldReturnEmpty_WhenNotFound() {
        //given
        Long movieId = 999L;
        when(movieRepository.findById(movieId)).thenReturn(Optional.empty());

        //when
        Optional<MovieResponse> actualResponse = movieService.findMovieById(movieId);

        //then
        assertThat(actualResponse).isEmpty();

        verify(movieRepository).findById(movieId);
        verify(movieMapper, never()).toDto(any());
    }
}
