package io.emkae.movieinfoservice.resource;

import io.emkae.movieinfoservice.model.Movie;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/movies")
@Log4j2
public class MovieResource {

    @RequestMapping("/{movieId}")
    public Movie getMovieInfo(@PathVariable("movieId") String movieId) {
        log.info("Requesting information for the movie : {}", movieId);
        Map<String, Movie> movieMap = getMovieMap();

        return movieMap.get(movieId);
    }

    private Map<String, Movie> getMovieMap() {
        Map<String, Movie> movieMap = new HashMap<>();

        movieMap.put("100", new Movie("100", "Transformers", "Transformers is a SciFi movie"));
        movieMap.put("101", new Movie("101", "Titanic", "Titanic is a romantic movie"));
        movieMap.put("102", new Movie("102", "Silent Place", "Silent place is a horror movie"));

        return movieMap;
    }
}
