package com.movie.shop.api.screening.domain.port;

import com.movie.shop.api.screening.domain.authorization.ScreeningRegistrationTheaterScope;

import java.util.Optional;

public interface ScreeningRegistrationTheaterScopePort {
    Optional<ScreeningRegistrationTheaterScope> findTheaterScope(long auditoriumId);
}
