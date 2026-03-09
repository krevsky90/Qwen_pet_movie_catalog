package com.krev.qwen_pet_movie_catalog.configuration;

public class CacheConstants {
    private CacheConstants() {}

    public static final String MOVIE_BY_ID = "movieById";
    public static final String MOVIES_LIST = "moviesList";
    public static final String MOVIES_SEARCH = "moviesSearch";

    public static final String KEY_PREFIX = "movie:";
    public static final String CACHE_DELIMITER = "::";
}
