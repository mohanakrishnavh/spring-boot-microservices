package io.emkae.movieinfoservice.resource;

import io.emkae.movieinfoservice.model.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice test for the controller only -- no full application context, no
 * Eureka client, so it doesn't log the (harmless but noisy)
 * "connection refused to localhost:8761" warnings a @SpringBootTest run
 * produces without a discovery server up.
 */
@WebMvcTest(MovieResource.class)
class MovieResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getMovieInfo_returnsTheHardcodedMovieForAKnownId() throws Exception {
        mockMvc.perform(get("/movies/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movieId", is("100")))
                .andExpect(jsonPath("$.name", is("Transformers")))
                .andExpect(jsonPath("$.description", is("Transformers is a SciFi movie")));
    }

    @Test
    void getMovieInfo_coversAllThreeHardcodedMovies() throws Exception {
        mockMvc.perform(get("/movies/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Titanic")));

        mockMvc.perform(get("/movies/102"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Silent Place")));
    }

    @Test
    void getMovieInfo_forAnUnknownId_returnsAnEmptyBodyRatherThanA404() throws Exception {
        // Documents the current, real behavior rather than the behavior
        // a caller might expect: getMovieMap().get(movieId) returns null
        // for an unrecognized ID, and Spring MVC serializes that as an
        // empty 200 OK body -- not a 404. If someone "fixes" this to
        // return 404 for unknown movies, that's a deliberate API change,
        // and this test should be updated deliberately alongside it.
        mockMvc.perform(get("/movies/999"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }
}
