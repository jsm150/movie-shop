package com.movie.shop.api.screening.domain.authorization;

import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;

import java.util.Objects;
import java.util.Optional;

public record ScreeningRegistrationTheaterScope(long theaterId) {
    public ScreeningRegistrationTheaterScope {
        if (theaterId <= 0) {
            throw new ScreeningDomainException("영화관 ID는 0보다 커야 합니다.");
        }
    }

    public static ScreeningRegistrationTheaterScope require(Optional<ScreeningRegistrationTheaterScope> theaterScope) {
        Objects.requireNonNull(theaterScope, "상영 등록 영화관 권한 범위는 필수입니다.");

        return theaterScope.orElseThrow(() -> new ScreeningDomainException("상영관 정보를 찾을 수 없습니다."));
    }
}
