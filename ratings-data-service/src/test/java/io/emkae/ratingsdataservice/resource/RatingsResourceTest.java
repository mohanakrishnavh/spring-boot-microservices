package io.emkae.ratingsdataservice.resource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RatingsResource.class)
class RatingsResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRatingByMovieId_returnsARating() throws Exception {
        // Documents the current, real behavior: getRatingByMovieId always
        // returns the same hardcoded Rating("100", "1000", 5) regardless
        // of the movieId in the path -- it's not actually looking up the
        // requested movie. That looks like a placeholder rather than a
        // finished implementation, but this project's other endpoints
        // (movie-info-service's 3-movie catalog, this service's 2-user
        // rating map) are all similarly hardcoded demo data, so it's not
        // obviously a bug versus intentional scaffolding. Asserted as-is;
        // if this becomes a real per-movie lookup, this test should
        // change to assert per-movieId results instead.
        mockMvc.perform(get("/ratings/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movieId", is("100")))
                .andExpect(jsonPath("$.userId", is("1000")))
                .andExpect(jsonPath("$.rating", is(5)));

        mockMvc.perform(get("/ratings/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movieId", is("100")));
    }

    @Test
    void getRatingByUserId_returnsThatUsersRatingsInOrder() throws Exception {
        mockMvc.perform(get("/ratings/user/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is("1001")))
                .andExpect(jsonPath("$.ratings", hasSize(3)))
                .andExpect(jsonPath("$.ratings[0].movieId", is("100")))
                .andExpect(jsonPath("$.ratings[0].rating", is(5)))
                .andExpect(jsonPath("$.ratings[2].movieId", is("102")))
                .andExpect(jsonPath("$.ratings[2].rating", is(3)));
    }

    @Test
    void getRatingByUserId_forAnUnknownUser_returnsNullRatingsList() throws Exception {
        // Same "documents reality" approach as MovieResourceTest's
        // unknown-ID case: ratingsByUserIdMap.get(userId) returns null
        // for an unrecognized user, so the response is
        // {"userId":"nobody","ratings":null} rather than an empty list
        // or a 404.
        mockMvc.perform(get("/ratings/user/nobody"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is("nobody")))
                .andExpect(jsonPath("$.ratings").doesNotExist());
    }
}
