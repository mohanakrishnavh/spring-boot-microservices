package io.emkae.ratingsdataservice.resource;

import io.emkae.ratingsdataservice.model.Rating;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ratings")
@Log4j2
public class RatingsResource {

    @RequestMapping("/{movieId}")
    public Rating getRating(@PathVariable("movieId") String movieId) {
       log.info("Requesting rating for the movie: {}", movieId);

       return new Rating(movieId, 5);
    }
}
