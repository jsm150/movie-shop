package com.movie.shop.api.screening.domain.policy.port;

import java.util.Optional;

public interface LoadTheaterScreeningAvailabilityPort {
    Optional<Boolean> loadTheaterScreeningAvailability(long theaterId);
}
