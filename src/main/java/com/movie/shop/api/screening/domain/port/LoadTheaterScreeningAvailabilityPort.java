package com.movie.shop.api.screening.domain.port;

import java.util.Optional;

public interface LoadTheaterScreeningAvailabilityPort {
    Optional<TheaterScreeningAvailability> loadTheaterScreeningAvailability(long theaterId);
}
