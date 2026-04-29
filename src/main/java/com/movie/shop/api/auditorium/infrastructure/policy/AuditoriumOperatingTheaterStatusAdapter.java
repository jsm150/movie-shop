package com.movie.shop.api.auditorium.infrastructure.policy;

import com.movie.shop.api.auditorium.domain.condition.AuditoriumOperatingTheaterStatus;
import com.movie.shop.api.auditorium.domain.port.AuditoriumOperatingTheaterStatusPort;
import com.movie.shop.api.theater.domain.port.TheaterJpaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuditoriumOperatingTheaterStatusAdapter implements AuditoriumOperatingTheaterStatusPort {

    private final TheaterJpaPort theaterJpaPort;

    @Override
    public Optional<AuditoriumOperatingTheaterStatus> findStatus(long theaterId) {
        return theaterJpaPort.findById(theaterId)
                .map(theater -> new AuditoriumOperatingTheaterStatus(theater.isActive()));
    }
}
