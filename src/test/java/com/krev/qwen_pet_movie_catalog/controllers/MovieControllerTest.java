package com.krev.qwen_pet_movie_catalog.controllers;

import com.krev.qwen_pet_movie_catalog.dto.MovieRequest;
import com.krev.qwen_pet_movie_catalog.dto.MovieResponse;
import com.krev.qwen_pet_movie_catalog.services.MovieService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@WebMvcTest(MovieController.class)
public class MovieControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper json;
    @MockitoBean
    private MovieService movieService;

    @Test
    void createMovie_WhouldReturn400_WhenYearIsTooOld() throws Exception {
        MovieRequest request = new MovieRequest(null, 180, "Comedy");
        //or Map<String, ? extends Serializable> requestData = Map.of("year", 180, "genre", "Comedy");

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/movies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors.year").value("Year too old"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors.title").value("Title is required"));

        verify(movieService, never()).createMovie(request);
    }

    @Test
    void createMovie_success() throws Exception {
        MovieRequest request = new MovieRequest("title1", 1995, "Comedy");
        MovieResponse response = new MovieResponse(34L, "title1", 1995, "Comedy", LocalDateTime.of(2026, 1, 15, 13, 0), LocalDateTime.of(2026, 1, 15, 13, 0));

        when(movieService.createMovie(request)).thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/movies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().exists("Location"))
                .andExpect(MockMvcResultMatchers.header().string("Location", Matchers.containsString("/api/v1/movies/34")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(34))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("title1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.year").value(1995))
                .andExpect(MockMvcResultMatchers.jsonPath("$.genre").value("Comedy"));

        verify(movieService, times(1)).createMovie(request);
    }

    //todo: write more tests
}
