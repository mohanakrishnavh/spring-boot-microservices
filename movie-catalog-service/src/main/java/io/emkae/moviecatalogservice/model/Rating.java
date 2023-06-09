package io.emkae.moviecatalogservice.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rating {
    private String movieId;
    private String userId;
    private int rating;
}
