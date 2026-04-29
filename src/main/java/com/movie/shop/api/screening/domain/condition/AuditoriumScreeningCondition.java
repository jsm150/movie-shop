package com.movie.shop.api.screening.domain.condition;

import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;

public record AuditoriumScreeningCondition(long theaterId, boolean canHostScreening) {
    public AuditoriumScreeningCondition {
        if (theaterId <= 0) {
            throw new ScreeningDomainException("영화관 ID는 0보다 커야 합니다.");
        }
    }
}
