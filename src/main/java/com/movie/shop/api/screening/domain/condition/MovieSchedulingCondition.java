package com.movie.shop.api.screening.domain.condition;

import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;

public record MovieSchedulingCondition(boolean canBeScheduled, int runtimeMinutes) {
    public MovieSchedulingCondition {
        if (runtimeMinutes <= 0) {
            throw new ScreeningDomainException("영화 런타임은 0보다 커야 합니다.");
        }
    }
}
