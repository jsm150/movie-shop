package com.movie.shop.api.screening.infrastructure.policy;

import com.movie.shop.api.screening.domain.policy.port.LoadTheaterScreeningAvailabilityPort;
import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.aggregate.port.TheaterJpaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LoadTheaterScreeningAvailabilityJpaAdapter implements LoadTheaterScreeningAvailabilityPort {

    private final TheaterJpaPort theaterJpaPort;

    @Override
    public Optional<Boolean> loadTheaterScreeningAvailability(long theaterId) {
        return theaterJpaPort.findById(theaterId).map(Theater::canHostScreening);
    }
}
