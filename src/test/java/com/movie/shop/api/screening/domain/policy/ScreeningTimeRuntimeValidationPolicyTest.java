package com.movie.shop.api.screening.domain.policy;

import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;
import com.movie.shop.api.screening.domain.policy.status.MovieSchedulingAvailability;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScreeningTimeRuntimeValidationPolicyTest {

    private static final int MOVIE_RUNTIME_MINUTES = 120;

    private final OffsetDateTime start = OffsetDateTime.parse("2026-02-10T10:00:00Z");

    @Test
    @DisplayName("상영 시간이 영화 런타임보다 짧으면 예외가 발생한다")
    void validateRuntime_withShorterThanRuntime_throwsException() {
        ScreeningTimeRuntimeValidationPolicy policy = newPolicy(Optional.of(movieAvailability(true, MOVIE_RUNTIME_MINUTES)));
        OffsetDateTime shortEnd = start.plusMinutes(MOVIE_RUNTIME_MINUTES - 1L);

        assertThatThrownBy(() -> policy.validateRuntime(start, shortEnd))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("영화 런타임(120분) 이상");
    }

    @Test
    @DisplayName("상영 시간이 영화 런타임과 같으면 검증에 성공한다")
    void validateRuntime_withEqualRuntime_succeeds() {
        ScreeningTimeRuntimeValidationPolicy policy = newPolicy(Optional.of(movieAvailability(true, MOVIE_RUNTIME_MINUTES)));
        OffsetDateTime runtimeEnd = start.plusMinutes(MOVIE_RUNTIME_MINUTES);

        assertThatCode(() -> policy.validateRuntime(start, runtimeEnd))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("상영 시간이 영화 런타임보다 길면 검증에 성공한다")
    void validateRuntime_withLongerThanRuntime_succeeds() {
        ScreeningTimeRuntimeValidationPolicy policy = newPolicy(Optional.of(movieAvailability(true, MOVIE_RUNTIME_MINUTES)));
        OffsetDateTime longEnd = start.plusMinutes(MOVIE_RUNTIME_MINUTES + 10L);

        assertThatCode(() -> policy.validateRuntime(start, longEnd))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("영화 정보를 찾을 수 없으면 예외가 발생한다")
    void validateRuntime_withMissingMovie_throwsException() {
        ScreeningTimeRuntimeValidationPolicy policy = newPolicy(Optional.empty());

        assertThatThrownBy(() -> policy.validateRuntime(start, start.plusMinutes(MOVIE_RUNTIME_MINUTES)))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("영화 정보를 찾을 수 없습니다.");
    }

    private ScreeningTimeRuntimeValidationPolicy newPolicy(Optional<MovieSchedulingAvailability> movieAvailability) {
        return new ScreeningTimeRuntimeValidationPolicy(movieAvailability);
    }

    private MovieSchedulingAvailability movieAvailability(boolean schedulable, int runtimeMinutes) {
        return new MovieSchedulingAvailability(schedulable, runtimeMinutes);
    }
}
