package io.emkae.moviecatalogservice.resources;

import io.emkae.moviecatalogservice.model.Movie;
import io.emkae.moviecatalogservice.model.Rating;
import io.emkae.moviecatalogservice.model.UserRating;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests MovieCatalogResource's fan-out: call ratings-data-service for a
 * user's ratings, then movie-info-service for each rated movie's details,
 * and combine them into CatalogItems. This is the actual point of the
 * movie-catalog-service and was previously covered only by a
 * contextLoads() stub.
 *
 * RestTemplate is mocked rather than hitting real services -- the
 * @LoadBalanced RestTemplate bean resolves service names ("ratings-data-
 * service", "movie-info-service") via Eureka, which isn't running in a
 * test. @WebMvcTest also means WebClient.Builder needs a @MockBean even
 * though this controller doesn't use it (Spring still needs to satisfy
 * the @Autowired field to build the web slice context).
 */
@WebMvcTest(MovieCatalogResource.class)
class MovieCatalogResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private WebClient.Builder webClientBuilder;

    @Test
    void getCatalog_combinesRatingsAndMovieInfoIntoCatalogItems() throws Exception {
        UserRating userRating = new UserRating("1000", Arrays.asList(
                new Rating("100", "1000", 5),
                new Rating("101", "1000", 4)
        ));
        when(restTemplate.getForObject(
                eq("http://ratings-data-service/ratings/user/1000"), eq(UserRating.class)))
                .thenReturn(userRating);
        when(restTemplate.getForObject(
                eq("http://movie-info-service/movies/100"), eq(Movie.class)))
                .thenReturn(new Movie("100", "Transformers", "Transformers is a SciFi movie"));
        when(restTemplate.getForObject(
                eq("http://movie-info-service/movies/101"), eq(Movie.class)))
                .thenReturn(new Movie("101", "Titanic", "Titanic is a romantic movie"));

        mockMvc.perform(get("/catalog/1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Transformers"))
                .andExpect(jsonPath("$[0].description").value("Transformers is a SciFi movie"))
                .andExpect(jsonPath("$[0].rating").value(5))
                .andExpect(jsonPath("$[1].name").value("Titanic"))
                .andExpect(jsonPath("$[1].rating").value(4));

        verify(restTemplate).getForObject("http://ratings-data-service/ratings/user/1000", UserRating.class);
        verify(restTemplate).getForObject("http://movie-info-service/movies/100", Movie.class);
        verify(restTemplate).getForObject("http://movie-info-service/movies/101", Movie.class);
    }

    @Test
    void getCatalog_whenRatingsServiceReturnsNothing_returnsAnEmptyCatalog() throws Exception {
        when(restTemplate.getForObject(
                eq("http://ratings-data-service/ratings/user/nobody"), eq(UserRating.class)))
                .thenReturn(null);

        mockMvc.perform(get("/catalog/nobody"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getCatalog_skipsRatingsWhoseMovieLookupReturnsNothing() throws Exception {
        // The controller's `if (movie != null)` guard should silently
        // drop a rating whose movie-info-service lookup comes back
        // empty, rather than including a CatalogItem with null fields
        // or failing the whole request.
        UserRating userRating = new UserRating("1000", Collections.singletonList(
                new Rating("999", "1000", 3)
        ));
        when(restTemplate.getForObject(
                eq("http://ratings-data-service/ratings/user/1000"), eq(UserRating.class)))
                .thenReturn(userRating);
        when(restTemplate.getForObject(
                eq("http://movie-info-service/movies/999"), eq(Movie.class)))
                .thenReturn(null);

        mockMvc.perform(get("/catalog/1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
