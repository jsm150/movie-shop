package com.movie.shop.api.screening.domain.aggregate;

import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;
import com.movie.shop.api.screening.domain.policy.ScreeningTimeRuntimeValidationPolicy;
import com.movie.shop.api.screening.domain.policy.status.MovieSchedulingAvailability;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScreeningTimeRangeTest {

    private static final int MOVIE_RUNTIME_MINUTES = 120;

    private final OffsetDateTime start = OffsetDateTime.parse("2026-02-10T10:00:00Z");

    @Test
    @DisplayName("상영 시간이 영화 런타임보다 짧으면 생성에 실패한다")
    void create_withShorterThanRuntime_throwsException() {
        ScreeningTimeRuntimeValidationPolicy runtimePolicy = runtimePolicy(true, MOVIE_RUNTIME_MINUTES);
        OffsetDateTime shortEnd = start.plusMinutes(MOVIE_RUNTIME_MINUTES - 1L);

        assertThatThrownBy(() -> ScreeningTimeRange.create(start, shortEnd, runtimePolicy))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("영화 런타임(120분) 이상");
    }

    @Test
    @DisplayName("상영 시간이 영화 런타임과 같으면 생성에 성공한다")
    void create_withEqualRuntime_succeeds() {
        ScreeningTimeRuntimeValidationPolicy runtimePolicy = runtimePolicy(true, MOVIE_RUNTIME_MINUTES);
        OffsetDateTime runtimeEnd = start.plusMinutes(MOVIE_RUNTIME_MINUTES);

        Validation<Seq<String>, ScreeningTimeRange> validation = ScreeningTimeRange.create(start, runtimeEnd, runtimePolicy);

        assertThat(validation.isValid()).isTrue();
    }

    @Test
    @DisplayName("상영 시작 시간이 종료 시간과 같거나 늦으면 시간 범위 메시지가 우선된다")
    void create_withInvalidRange_returnsRangeErrorFirst() {
        ScreeningTimeRuntimeValidationPolicy runtimePolicy = runtimePolicy(true, MOVIE_RUNTIME_MINUTES);

        Validation<Seq<String>, ScreeningTimeRange> validation = ScreeningTimeRange.create(start, start, runtimePolicy);

        assertThat(validation.isInvalid()).isTrue();
        assertThat(validation.getError().mkString(","))
                .contains("상영 시작 시간은 상영 종료 시간 이전 이여야 합니다.")
                .doesNotContain("영화 런타임");
    }

    private ScreeningTimeRuntimeValidationPolicy runtimePolicy(boolean schedulable, int runtimeMinutes) {
        return new ScreeningTimeRuntimeValidationPolicy(
                Optional.of(new MovieSchedulingAvailability(schedulable, runtimeMinutes))
        );
    }
}
