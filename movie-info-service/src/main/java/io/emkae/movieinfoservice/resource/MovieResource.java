package io.emkae.movieinfoservice.resource;

import io.emkae.movieinfoservice.model.Movie;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/movies")
@Log4j2
public class MovieResource {

    @RequestMapping("/{movieId}")
    public Movie getMovieInfo(@PathVariable("movieId") String movieId) {
        log.info("Requesting information for the movie : {}", movieId);

        return new Movie(movieId, "Test name");
    }
}
