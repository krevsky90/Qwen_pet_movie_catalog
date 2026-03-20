package com.krev.qwen_pet_movie_catalog.external.omdb;

import com.krev.qwen_pet_movie_catalog.configuration.properties.OmdbProperties;
import com.krev.qwen_pet_movie_catalog.external.omdb.dto.OmdbResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OmdbGateway {
    private final static Logger LOGGER = LoggerFactory.getLogger(OmdbGateway.class);

    private final OmdbClient omdbClient;
    private final OmdbProperties omdbProperties;

    public OmdbGateway(OmdbClient omdbClient, OmdbProperties omdbProperties) {
        this.omdbClient = omdbClient;
        this.omdbProperties = omdbProperties;
    }

    //Fallback должен быть ТОЛЬКО на внешнем слое (обычно Retry)
    // NOTE: последовательность аннотаций НЕ гарантирует, что АОП будет оборачивать именно в таком порядке!
    @Retry(name = "omdbApi", fallbackMethod = "getMovieFallback")
    @CircuitBreaker(name = "omdbApi")
    public OmdbResponse getMovieByTitleAndYear(String title, Integer year) {
        LOGGER.info("Calling OMDB...");
        return omdbClient.getMovieByTitleAndYear(omdbProperties.apiKey(), title, year);
//        throw new RuntimeException("Test Retry + CircuitBreaker"); <-- for test
    }

    //signature = signature of original method + Throwable throwable
    public OmdbResponse getMovieFallback(String title, Integer year, Throwable throwable) {
        LOGGER.warn("Fallback for OMdb call: {} ({})", title, year, throwable.getMessage());
        return new OmdbResponse(
                title,
                year.toString(),
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
    }
}
