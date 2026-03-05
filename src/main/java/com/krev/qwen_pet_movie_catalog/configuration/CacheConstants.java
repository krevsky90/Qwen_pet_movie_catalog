package com.krev.qwen_pet_movie_catalog.configuration;

import java.time.Duration;

public class CacheConstants {
    private CacheConstants() {}

    public static final String MOVIE_BY_ID = "movieById";
    public static final String MOVIES_LIST = "moviesList";
    public static final String MOVIES_SEARCH = "moviesSearch";

    public static final Duration TTL_BY_ID = Duration.ofMinutes(10);
    public static final Duration TTL_LIST_SEARCH = Duration.ofMinutes(5);

    public static final String KEY_PREFIX = "movie:";
}
