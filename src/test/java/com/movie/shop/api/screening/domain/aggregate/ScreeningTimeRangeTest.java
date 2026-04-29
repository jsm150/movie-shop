package com.movie.shop.api.screening.domain.aggregate;

import com.movie.shop.api.screening.domain.condition.MovieSchedulingCondition;
import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScreeningTimeRangeTest {

    private static final int MOVIE_RUNTIME_MINUTES = 120;

    private final OffsetDateTime start = OffsetDateTime.parse("2026-02-10T10:00:00Z");

    @Test
    @DisplayName("상영 시간이 영화 런타임보다 짧으면 생성에 실패한다")
    void create_withShorterThanRuntime_throwsException() {
        MovieSchedulingCondition movieCondition = movieCondition(MOVIE_RUNTIME_MINUTES);
        OffsetDateTime shortEnd = start.plusMinutes(MOVIE_RUNTIME_MINUTES - 1L);

        assertThatThrownBy(() -> ScreeningTimeRange.create(start, shortEnd, movieCondition))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("영화 런타임(120분) 이상");
    }

    @Test
    @DisplayName("상영 시간이 영화 런타임과 같으면 생성에 성공한다")
    void create_withEqualRuntime_succeeds() {
        MovieSchedulingCondition movieCondition = movieCondition(MOVIE_RUNTIME_MINUTES);
        OffsetDateTime runtimeEnd = start.plusMinutes(MOVIE_RUNTIME_MINUTES);

        ScreeningTimeRange timeRange = ScreeningTimeRange.create(start, runtimeEnd, movieCondition);

        assertThat(timeRange.getStartTime()).isEqualTo(start);
        assertThat(timeRange.getEndTime()).isEqualTo(runtimeEnd);
    }

    @Test
    @DisplayName("상영 시작/종료 시간이 null이면 생성 시점에 검증 오류를 수집한다")
    void create_withNullTimes_collectsValidationErrors() {
        MovieSchedulingCondition movieCondition = movieCondition(MOVIE_RUNTIME_MINUTES);

        assertThatThrownBy(() -> ScreeningTimeRange.create(null, null, movieCondition))
                .isInstanceOf(ScreeningDomainException.class)
                .satisfies(exception -> assertThat(
                        ((ScreeningDomainException) exception).getErrors()
                ).contains(
                        "상영 시작 시간이 필요합니다.",
                        "상영 종료 시간이 필요합니다."
                ));
    }

    @Test
    @DisplayName("영화 상영 조건이 null이면 생성에 실패한다")
    void create_withNullMovieCondition_throwsException() {
        OffsetDateTime runtimeEnd = start.plusMinutes(MOVIE_RUNTIME_MINUTES);

        assertThatThrownBy(() -> ScreeningTimeRange.create(start, runtimeEnd, null))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("영화 상영 조건은 필수입니다.");
    }

    @Test
    @DisplayName("상영 시작 시간이 종료 시간과 같거나 늦으면 시간 범위 메시지가 우선된다")
    void create_withInvalidRange_throwsRangeErrorFirst() {
        MovieSchedulingCondition movieCondition = movieCondition(MOVIE_RUNTIME_MINUTES);

        assertThatThrownBy(() -> ScreeningTimeRange.create(start, start, movieCondition))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("상영 시작 시간은 상영 종료 시간 이전 이여야 합니다.")
                .satisfies(exception -> assertThat(exception.getMessage())
                        .doesNotContain("영화 런타임"));
    }

    private MovieSchedulingCondition movieCondition(int runtimeMinutes) {
        return new MovieSchedulingCondition(true, runtimeMinutes);
    }
}
