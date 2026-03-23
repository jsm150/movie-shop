package com.movie.shop.api.theater.domain.policy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.movie.shop.api.theater.domain.port.TheaterJpaPort;

@Component
@RequiredArgsConstructor
public class TheaterNameDuplicateValidator {

    private final TheaterJpaPort theaterJpaPort;

    public boolean validateNotDuplicate(String theaterName) {
        return !theaterJpaPort.existsByName(theaterName);
    }
}
