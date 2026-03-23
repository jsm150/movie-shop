package com.movie.shop.api.theater.infrastructure.policy;

import com.movie.shop.api.auditorium.domain.port.AuditoriumJpaPort;
import com.movie.shop.api.theater.domain.policy.status.TheaterAuditoriumLinkStatus;
import com.movie.shop.api.theater.domain.port.CheckTheaterAuditoriumLinkPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckTheaterAuditoriumLinkJpaAdapter implements CheckTheaterAuditoriumLinkPort {

    private final AuditoriumJpaPort auditoriumJpaPort;

    @Override
    public TheaterAuditoriumLinkStatus loadTheaterAuditoriumLinkStatus(long theaterId) {
        boolean linked = auditoriumJpaPort.existsByTheaterId(theaterId);

        return new TheaterAuditoriumLinkStatus(linked);
    }
}
