package com.movie.shop.api.screening.domain.port;

import java.util.Optional;

public interface LoadTheaterScreeningAvailabilityPort {
    Optional<Boolean> loadTheaterScreeningAvailability(long theaterId);
}
