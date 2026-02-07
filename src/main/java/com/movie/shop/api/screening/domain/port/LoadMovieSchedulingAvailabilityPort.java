package com.movie.shop.api.screening.domain.port;

import java.util.Optional;

public interface LoadMovieSchedulingAvailabilityPort {
    Optional<MovieSchedulingAvailability> loadMovieSchedulingAvailability(long movieId);
}
