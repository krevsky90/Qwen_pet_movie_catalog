package com.krev.qwen_pet_movie_catalog.external.omdb;

import com.krev.qwen_pet_movie_catalog.external.omdb.dto.OmdbResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "omdb-api",
        url = "${external.omdb.base-url}",
        configuration = OmdbClientConfig.class
)
public interface OmdbClient {
    //Examples by Query Type
    //Request Type 	            Parameter(s)
    //Search by title	        t (title)
    //Search by IMDb ID	        i (IMDb ID)
    //Search by keyword	        s (search)
    //Specify year	            y (year)
    //Specify plot length	    plot (short/full)
    //Get series season info	i + Season

    //forms request GET https://www.omdbapi.com/?apiKey=<my_api_key>&t=<my_title>?&y=<my_year>
    @GetMapping(value = "/", params = {"apiKey", "t", "y"})
    OmdbResponse getMovieByTitleAndYear(
            @RequestParam("apiKey") String apiKey,
            @RequestParam("t") String title,
            @RequestParam("y") Integer year
    );
}
