package com.movie.shop.api.screening.domain.policy;

import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

public record ScreeningTimeRuntimeValidationPolicy(Optional<MovieSchedulingAvailability> movieSchedulingAvailability) {

    public void validateRuntime(OffsetDateTime start, OffsetDateTime end) {
        MovieSchedulingAvailability movie = movieSchedulingAvailability
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
