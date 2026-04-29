package com.movie.shop.api.auditorium.infrastructure.policy;

import com.movie.shop.api.auditorium.domain.condition.AuditoriumRegistrationTheater;
import com.movie.shop.api.auditorium.domain.port.AuditoriumRegistrationTheaterPort;
import com.movie.shop.api.theater.domain.port.TheaterJpaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuditoriumRegistrationTheaterAdapter implements AuditoriumRegistrationTheaterPort {

    private final TheaterJpaPort theaterJpaPort;

    @Override
    public Optional<AuditoriumRegistrationTheater> findRegistrationTheater(long theaterId) {
        return theaterJpaPort.findById(theaterId)
                .map(theater -> new AuditoriumRegistrationTheater(theater.getId()));
    }
}
