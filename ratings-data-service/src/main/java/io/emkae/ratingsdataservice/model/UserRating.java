package io.emkae.ratingsdataservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UserRating {
    private String userId;
    private List<Rating> ratings;
}
