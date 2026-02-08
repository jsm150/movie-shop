package com.movie.shop.api.screening.domain.port;

import com.movie.shop.api.screening.domain.policy.TheaterScreeningAvailability;

import java.util.Optional;

public interface LoadTheaterScreeningAvailabilityPort {
    Optional<TheaterScreeningAvailability> loadTheaterScreeningAvailability(long theaterId);
}
