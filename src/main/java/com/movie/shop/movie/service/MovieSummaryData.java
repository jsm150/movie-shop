package com.movie.shop.movie.service;

import com.movie.shop.movie.domain.MovieReadModel;
import lombok.Getter;

@Getter
public class MovieSummaryData {
    private Long movieId;
    private String title;
    private String posterImageUrl;
    private String audienceRating;
    private Double bookingRate;
    private Double averageRating;

    public MovieSummaryData(MovieReadModel entity) {
        this.movieId = entity.getMovieId();
        this.title = entity.getTitle();
        this.posterImageUrl = entity.getPosterImageUrl();
        this.audienceRating = entity.getAudienceRating();
        this.bookingRate = entity.getBookingRate();
        this.averageRating = entity.getAverageRating();
    }
}