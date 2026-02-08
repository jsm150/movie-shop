package com.movie.shop.api.screening.infrastructure.policy;

import com.movie.shop.api.screening.domain.port.LoadTheaterScreeningAvailabilityPort;
import com.movie.shop.api.screening.domain.policy.TheaterScreeningAvailability;
import com.movie.shop.api.theater.domain.port.TheaterJpaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LoadTheaterScreeningAvailabilityJpaAdapter implements LoadTheaterScreeningAvailabilityPort {

    private final TheaterJpaPort theaterJpaPort;

    @Override
    public Optional<TheaterScreeningAvailability> loadTheaterScreeningAvailability(long theaterId) {
        return theaterJpaPort.findById(theaterId)
                .map(theater -> new TheaterScreeningAvailability(theater.canHostScreening()));
    }
}
