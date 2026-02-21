package com.movie.shop.api.screening.domain.policy;

import com.movie.shop.api.screening.domain.aggregate.Screening;
import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;

import java.time.OffsetDateTime;
import java.util.List;

public record ScreeningConflictValidationPolicy(List<Screening> conflictCandidates) {

    public void validateNoConflict(OffsetDateTime screeningStart, OffsetDateTime screeningEnd) {
        boolean hasConflict = conflictCandidates
                .stream()
                .anyMatch(screening -> screening.hasTimeConflictWith(screeningStart, screeningEnd));

        if (hasConflict) {
            throw new ScreeningDomainException("동일한 극장에 상영 시간이 겹치는 일정이 존재합니다.");
        }
    }
}
