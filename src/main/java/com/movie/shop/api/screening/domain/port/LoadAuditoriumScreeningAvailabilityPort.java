package com.movie.shop.api.screening.domain.port;

import java.util.Optional;

import com.movie.shop.api.screening.domain.policy.status.AuditoriumScreeningAvailability;

public interface LoadAuditoriumScreeningAvailabilityPort {
    Optional<AuditoriumScreeningAvailability> loadTheaterScreeningAvailability(long auditoriumId);
}
