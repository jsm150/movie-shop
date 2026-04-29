package com.movie.shop.api.auditorium.domain.condition;

import com.movie.shop.api.auditorium.domain.exceptions.AuditoriumDomainException;

public record AuditoriumRegistrationTheater(long theaterId) {
    public AuditoriumRegistrationTheater {
        if (theaterId <= 0) {
            throw new AuditoriumDomainException("영화관 ID는 0보다 커야 합니다.");
        }
    }
}
