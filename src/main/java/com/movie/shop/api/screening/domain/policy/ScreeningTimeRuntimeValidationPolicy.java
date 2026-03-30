package com.movie.shop.api.screening.domain.policy;

import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;
import com.movie.shop.api.screening.domain.policy.status.MovieSchedulingAvailability;
import com.movie.shop.api.screening.domain.port.LoadMovieSchedulingAvailabilityPort;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;

public record ScreeningTimeRuntimeValidationPolicy(LoadMovieSchedulingAvailabilityPort loadMovieSchedulingAvailabilityPort) {

    public ScreeningTimeRuntimeValidationPolicy {
        Objects.requireNonNull(loadMovieSchedulingAvailabilityPort, "영화 상영 가능 정보 조회 포트는 필수입니다.");
    }

    public void validateRuntime(long movieId, OffsetDateTime start, OffsetDateTime end) {
        MovieSchedulingAvailability movie = loadMovieSchedulingAvailabilityPort.loadMovieSchedulingAvailability(movieId)
                .orElseThrow(() -> new ScreeningDomainException("영화 정보를 찾을 수 없습니다."));

        Duration screeningDuration = Duration.between(start, end);
        Duration runtimeDuration = Duration.ofMinutes(movie.runtimeMinutes());

        if (screeningDuration.compareTo(runtimeDuration) < 0) {
            throw new ScreeningDomainException(
                    "상영 시간은 영화 런타임(%d분) 이상이어야 합니다.".formatted(movie.runtimeMinutes())
            );
        }
    }
}
