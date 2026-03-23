package com.movie.shop.api.screening.infrastructure.policy;

import com.movie.shop.api.screening.domain.policy.status.AuditoriumScreeningAvailability;
import com.movie.shop.api.screening.domain.port.LoadAuditoriumScreeningAvailabilityPort;
import com.movie.shop.api.auditorium.domain.port.AuditoriumJpaPort;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LoadAuditoriumScreeningAvailabilityJpaAdapter implements LoadAuditoriumScreeningAvailabilityPort {

    private final AuditoriumJpaPort auditoriumJpaPort;

    @Override
    public Optional<AuditoriumScreeningAvailability> loadTheaterScreeningAvailability(long auditoriumId) {
        return auditoriumJpaPort.findById(auditoriumId)
                .map(auditorium -> new AuditoriumScreeningAvailability(auditorium.canHostScreening()));
    }
}
