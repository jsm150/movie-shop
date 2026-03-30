package com.movie.shop.api.screening.domain.port;

import java.util.Optional;

public interface LoadAuditoriumScreeningAvailabilityPort {
    Optional<Boolean> loadAuditoriumScreeningAvailability(long auditoriumId);
}
