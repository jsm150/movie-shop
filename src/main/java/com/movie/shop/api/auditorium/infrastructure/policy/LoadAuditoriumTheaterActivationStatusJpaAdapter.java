package com.movie.shop.api.auditorium.infrastructure.policy;

import com.movie.shop.api.auditorium.domain.policy.status.AuditoriumTheaterActivationStatus;
import com.movie.shop.api.auditorium.domain.port.LoadAuditoriumTheaterActivationStatusPort;
import com.movie.shop.api.theater.domain.port.TheaterJpaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LoadAuditoriumTheaterActivationStatusJpaAdapter implements LoadAuditoriumTheaterActivationStatusPort {

    private final TheaterJpaPort theaterJpaPort;

    @Override
    public Optional<AuditoriumTheaterActivationStatus> loadAuditoriumTheaterActivationStatus(long theaterId) {
        return theaterJpaPort.findById(theaterId)
                .map(theater -> new AuditoriumTheaterActivationStatus(theater.isActive()));
    }
}
