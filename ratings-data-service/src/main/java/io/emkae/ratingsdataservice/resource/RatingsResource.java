package io.emkae.ratingsdataservice.resource;

import io.emkae.ratingsdataservice.model.Rating;
import io.emkae.ratingsdataservice.model.UserRating;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ratings")
@Log4j2
public class RatingsResource {

    @RequestMapping("/{movieId}")
    public Rating getRatingByMovieId(@PathVariable("movieId") String movieId) {
       log.info("Requesting rating for the movie: {}", movieId);

       return new Rating("100", "1000", 5);
    }

    @RequestMapping("/user/{userId}")
    public UserRating getRatingByUserId(@PathVariable("userId") String userId) {
        log.info("Requesting rating for the user: {}", userId);

        Map<String, List<Rating>> ratingsByUserIdMap = getRatingsByUserIdMap();

        return new UserRating(userId, ratingsByUserIdMap.get(userId));
    }

    private Map<String, List<Rating>> getRatingsByUserIdMap() {
        Map<String, List<Rating>> ratingsByUserIdMap = new HashMap<>();
        ratingsByUserIdMap.put("1000", Arrays.asList(
            new Rating("100", "1000", 5),
            new Rating("101", "1000", 5)
        ));

        ratingsByUserIdMap.put("1001", Arrays.asList(
            new Rating("100", "1001", 5),
            new Rating("101", "1001", 4),
            new Rating("102", "1001", 3)
        ));

        return ratingsByUserIdMap;
    }
}
