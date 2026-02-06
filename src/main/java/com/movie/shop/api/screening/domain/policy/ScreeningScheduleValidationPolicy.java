package com.movie.shop.api.screening.domain.policy;

import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;
import com.movie.shop.api.screening.domain.policy.port.CheckScreeningTimeConflictPort;
import com.movie.shop.api.screening.domain.policy.port.LoadMovieSchedulingAvailabilityPort;
import com.movie.shop.api.screening.domain.policy.port.LoadTheaterScreeningAvailabilityPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class ScreeningScheduleValidationPolicy {

    private final LoadMovieSchedulingAvailabilityPort loadMovieSchedulingAvailabilityPort;
    private final LoadTheaterScreeningAvailabilityPort loadTheaterScreeningAvailabilityPort;
    private final CheckScreeningTimeConflictPort checkScreeningTimeConflictPort;

    public void validateCanCreateScreeningSchedule(long movieId,
                                                   long theaterId,
                                                   OffsetDateTime screeningStart,
                                                   OffsetDateTime screeningEnd) {
        validateMovie(movieId);
        validateTheater(theaterId);
        validateNoConflict(theaterId, screeningStart, screeningEnd);
    }

    public void validateCanRescheduleScreening(long screeningId,
                                               long movieId,
                                               long theaterId,
                                               OffsetDateTime screeningStart,
                                               OffsetDateTime screeningEnd) {
        validateMovie(movieId);
        validateTheater(theaterId);
        validateNoConflictExcluding(screeningId, theaterId, screeningStart, screeningEnd);
    }

    private void validateMovie(long movieId) {
        boolean movieAvailable = loadMovieSchedulingAvailabilityPort.loadMovieSchedulingAvailability(movieId)
                .orElseThrow(() -> new ScreeningDomainException("영화 정보를 찾을 수 없습니다."));

        if (!movieAvailable) {
            throw new ScreeningDomainException("상영 등록/수정은 COMING_SOON 또는 NOW_SHOWING 상태의 영화만 가능합니다.");
        }
    }

    private void validateTheater(long theaterId) {
        boolean theaterAvailable = loadTheaterScreeningAvailabilityPort.loadTheaterScreeningAvailability(theaterId)
                .orElseThrow(() -> new ScreeningDomainException("극장 정보를 찾을 수 없습니다."));

        if (!theaterAvailable) {
            throw new ScreeningDomainException("활성화된 극장에서만 상영 등록/수정이 가능합니다.");
        }
    }

    private void validateNoConflict(long theaterId, OffsetDateTime screeningStart, OffsetDateTime screeningEnd) {
        if (checkScreeningTimeConflictPort.hasConflict(theaterId, screeningStart, screeningEnd)) {
            throw new ScreeningDomainException("동일한 극장에 상영 시간이 겹치는 일정이 존재합니다.");
        }
    }

    private void validateNoConflictExcluding(long screeningId,
                                             long theaterId,
                                             OffsetDateTime screeningStart,
                                             OffsetDateTime screeningEnd) {
        if (checkScreeningTimeConflictPort.hasConflictExcluding(screeningId, theaterId, screeningStart, screeningEnd)) {
            throw new ScreeningDomainException("동일한 극장에 상영 시간이 겹치는 일정이 존재합니다.");
        }
    }
}
