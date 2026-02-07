package com.movie.shop.api.screening.domain.policy;

import com.movie.shop.api.screening.domain.port.ScreeningJpaPort;
import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;
import com.movie.shop.api.screening.domain.port.LoadMovieSchedulingAvailabilityPort;
import com.movie.shop.api.screening.domain.port.LoadTheaterScreeningAvailabilityPort;
import com.movie.shop.api.screening.domain.port.MovieSchedulingAvailability;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class ScreeningScheduleValidationPolicy {

    private final LoadMovieSchedulingAvailabilityPort loadMovieSchedulingAvailabilityPort;
    private final LoadTheaterScreeningAvailabilityPort loadTheaterScreeningAvailabilityPort;
    private final ScreeningJpaPort screeningJpaPort;

    public void validateCanCreateScreeningSchedule(long movieId,
                                                   long theaterId,
                                                   OffsetDateTime screeningStart,
                                                   OffsetDateTime screeningEnd) {
        validateMovie(movieId, screeningStart, screeningEnd);
        validateTheater(theaterId);
        validateNoConflict(theaterId, screeningStart, screeningEnd);
    }

    public void validateCanRescheduleScreening(long screeningId,
                                               long movieId,
                                               long theaterId,
                                               OffsetDateTime screeningStart,
                                               OffsetDateTime screeningEnd) {
        validateMovie(movieId, screeningStart, screeningEnd);
        validateTheater(theaterId);
        validateNoConflictExcluding(screeningId, theaterId, screeningStart, screeningEnd);
    }

    private void validateMovie(long movieId, OffsetDateTime screeningStart, OffsetDateTime screeningEnd) {
        MovieSchedulingAvailability movieSchedulingAvailability = loadMovieSchedulingAvailabilityPort.loadMovieSchedulingAvailability(movieId)
                .orElseThrow(() -> new ScreeningDomainException("영화 정보를 찾을 수 없습니다."));

        if (!movieSchedulingAvailability.schedulable()) {
            throw new ScreeningDomainException("상영 등록/수정은 COMING_SOON 또는 NOW_SHOWING 상태의 영화만 가능합니다.");
        }

        if (!canValidateRuntime(screeningStart, screeningEnd)) {
            return;
        }

        Duration screeningDuration = Duration.between(screeningStart, screeningEnd);
        Duration runtimeDuration = Duration.ofMinutes(movieSchedulingAvailability.runtimeMinutes());

        if (screeningDuration.compareTo(runtimeDuration) < 0) {
            throw new ScreeningDomainException(
                    "상영 시간은 영화 런타임(%d분) 이상이어야 합니다.".formatted(movieSchedulingAvailability.runtimeMinutes())
            );
        }
    }

    private boolean canValidateRuntime(OffsetDateTime screeningStart, OffsetDateTime screeningEnd) {
        return screeningStart != null
                && screeningEnd != null
                && screeningStart.isBefore(screeningEnd);
    }

    private void validateTheater(long theaterId) {
        boolean theaterAvailable = loadTheaterScreeningAvailabilityPort.loadTheaterScreeningAvailability(theaterId)
                .orElseThrow(() -> new ScreeningDomainException("극장 정보를 찾을 수 없습니다."));

        if (!theaterAvailable) {
            throw new ScreeningDomainException("활성화된 극장에서만 상영 등록/수정이 가능합니다.");
        }
    }

    private void validateNoConflict(long theaterId, OffsetDateTime screeningStart, OffsetDateTime screeningEnd) {
        boolean hasConflict = screeningJpaPort.findConflictCandidatesByTheaterId(theaterId, screeningStart, screeningEnd)
                .stream()
                .anyMatch(screening -> screening.hasTimeConflictWith(screeningStart, screeningEnd));

        if (hasConflict) {
            throw new ScreeningDomainException("동일한 극장에 상영 시간이 겹치는 일정이 존재합니다.");
        }
    }

    private void validateNoConflictExcluding(long screeningId,
                                             long theaterId,
                                             OffsetDateTime screeningStart,
                                             OffsetDateTime screeningEnd) {
        boolean hasConflict = screeningJpaPort.findConflictCandidatesByTheaterIdAndIdNot(
                        theaterId,
                        screeningStart,
                        screeningEnd,
                        screeningId
                )
                .stream()
                .anyMatch(screening -> screening.hasTimeConflictWith(screeningStart, screeningEnd));

        if (hasConflict) {
            throw new ScreeningDomainException("동일한 극장에 상영 시간이 겹치는 일정이 존재합니다.");
        }
    }
}
