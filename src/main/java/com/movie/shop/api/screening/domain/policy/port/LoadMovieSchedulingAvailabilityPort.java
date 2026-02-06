package com.movie.shop.api.screening.domain.policy.port;

import java.util.Optional;

public interface LoadMovieSchedulingAvailabilityPort {
    Optional<Boolean> loadMovieSchedulingAvailability(long movieId);
}
