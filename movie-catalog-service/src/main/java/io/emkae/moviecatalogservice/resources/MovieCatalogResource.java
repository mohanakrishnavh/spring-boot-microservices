package io.emkae.moviecatalogservice.resources;

import io.emkae.moviecatalogservice.model.CatalogItem;
import io.emkae.moviecatalogservice.model.Movie;
import io.emkae.moviecatalogservice.model.Rating;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/catalog")
@Log4j2
public class MovieCatalogResource {

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("/{userId}")
    public List<CatalogItem> getCatalog(@PathVariable("userId") String userId) {
        log.info("Request catalog information for user : {}", userId);

        List<Rating> ratings = Arrays.asList(
                new Rating("100", 4),
                new Rating("101", 3),
                new Rating("102", 5)
        );

        List<CatalogItem> catalogItems = new ArrayList<>();
        for (Rating rating : ratings) {
            Movie movie = restTemplate.getForObject("http://localhost:8082/movies/" + rating.getMovieId(), Movie.class);
            if (movie != null) {
                catalogItems.add(new CatalogItem(movie.getName(), movie.getDescription(), rating.getRating()));
            }
        }

        return catalogItems;
    }
}
