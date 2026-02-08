package com.movie.shop.api.screening.domain.port;

import com.movie.shop.api.screening.domain.policy.MovieSchedulingAvailability;

import java.util.Optional;

public interface LoadMovieSchedulingAvailabilityPort {
    Optional<MovieSchedulingAvailability> loadMovieSchedulingAvailability(long movieId);
}
