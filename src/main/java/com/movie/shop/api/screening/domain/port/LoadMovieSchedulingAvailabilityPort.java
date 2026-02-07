package com.movie.shop.api.screening.domain.port;

import java.util.Optional;

public interface LoadMovieSchedulingAvailabilityPort {
    Optional<Boolean> loadMovieSchedulingAvailability(long movieId);
}
