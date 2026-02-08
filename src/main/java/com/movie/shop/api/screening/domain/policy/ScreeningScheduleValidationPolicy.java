package com.movie.shop.api.screening.domain.policy;

import com.movie.shop.api.screening.domain.aggregate.Screening;
import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public record ScreeningScheduleValidationPolicy(Optional<MovieSchedulingAvailability> movieSchedulingAvailability,
                                                Optional<TheaterScreeningAvailability> theaterScreeningAvailability,
                                                List<Screening> conflictCandidates) {

    public void validateCanCreateScreeningSchedule(OffsetDateTime screeningStart,
                                                   OffsetDateTime screeningEnd) {
        validateMovie();
        validateTheater();
        validateNoConflict(screeningStart, screeningEnd);
    }

    public void validateCanRescheduleScreening(OffsetDateTime screeningStart,
                                               OffsetDateTime screeningEnd) {
        validateMovie();
        validateTheater();
        validateNoConflict(screeningStart, screeningEnd);
    }

    private void validateMovie() {
        MovieSchedulingAvailability movie = movieSchedulingAvailability
                .orElseThrow(() -> new ScreeningDomainException("영화 정보를 찾을 수 없습니다."));

        if (!movie.schedulable()) {
            throw new ScreeningDomainException("상영 등록/수정은 COMING_SOON 또는 NOW_SHOWING 상태의 영화만 가능합니다.");
        }
    }

    private void validateTheater() {
        TheaterScreeningAvailability theater = theaterScreeningAvailability
                .orElseThrow(() -> new ScreeningDomainException("극장 정보를 찾을 수 없습니다."));

        if (!theater.available()) {
            throw new ScreeningDomainException("활성화된 극장에서만 상영 등록/수정이 가능합니다.");
        }
    }

    private void validateNoConflict(OffsetDateTime screeningStart, OffsetDateTime screeningEnd) {
        boolean hasConflict = conflictCandidates
                .stream()
                .anyMatch(screening -> screening.hasTimeConflictWith(screeningStart, screeningEnd));

        if (hasConflict) {
            throw new ScreeningDomainException("동일한 극장에 상영 시간이 겹치는 일정이 존재합니다.");
        }
    }
}
