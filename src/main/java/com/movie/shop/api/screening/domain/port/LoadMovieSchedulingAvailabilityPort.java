package com.movie.shop.api.screening.domain.port;

import java.util.Optional;

import com.movie.shop.api.screening.domain.policy.status.MovieSchedulingAvailability;

public interface LoadMovieSchedulingAvailabilityPort {
    Optional<MovieSchedulingAvailability> loadMovieSchedulingAvailability(long movieId);
}
