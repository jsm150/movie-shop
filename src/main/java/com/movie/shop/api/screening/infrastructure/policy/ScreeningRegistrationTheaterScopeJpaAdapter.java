package com.movie.shop.api.screening.infrastructure.policy;

import com.movie.shop.api.auditorium.domain.port.AuditoriumJpaPort;
import com.movie.shop.api.screening.domain.authorization.ScreeningRegistrationTheaterScope;
import com.movie.shop.api.screening.domain.port.ScreeningRegistrationTheaterScopePort;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ScreeningRegistrationTheaterScopeJpaAdapter implements ScreeningRegistrationTheaterScopePort {

    private final AuditoriumJpaPort auditoriumJpaPort;

    @Override
    public Optional<ScreeningRegistrationTheaterScope> findTheaterScope(long auditoriumId) {
        return auditoriumJpaPort.findById(auditoriumId)
                .map(auditorium -> new ScreeningRegistrationTheaterScope(auditorium.getTheaterId()));
    }
}
