package io.emkae.ratingsdataservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Rating {
    private String movieId;
    private String userId;
    private int rating;
}
