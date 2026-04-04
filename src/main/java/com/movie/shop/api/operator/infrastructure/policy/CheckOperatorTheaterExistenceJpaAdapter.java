package com.movie.shop.api.operator.infrastructure.policy;

import org.springframework.stereotype.Component;

import com.movie.shop.api.operator.domain.port.CheckOperatorTheaterExistencePort;
import com.movie.shop.api.theater.domain.port.TheaterJpaPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CheckOperatorTheaterExistenceJpaAdapter implements CheckOperatorTheaterExistencePort {

    private final TheaterJpaPort theaterJpaPort;

    @Override
    public boolean existsTheater(long theaterId) {
        return theaterJpaPort.findById(theaterId).isPresent();
    }
}
