package com.movie.shop.api.theater.infrastructure.policy;

import com.movie.shop.api.auditorium.domain.port.AuditoriumJpaPort;
import com.movie.shop.api.theater.domain.condition.TheaterAuditoriumPresence;
import com.movie.shop.api.theater.domain.port.TheaterAuditoriumPresencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TheaterAuditoriumPresenceJpaAdapter implements TheaterAuditoriumPresencePort {

    private final AuditoriumJpaPort auditoriumJpaPort;

    @Override
    public TheaterAuditoriumPresence findPresence(long theaterId) {
        return new TheaterAuditoriumPresence(auditoriumJpaPort.existsByTheaterId(theaterId));
    }
}
