package com.movie.shop.api.screening.domain.policy;

import com.movie.shop.api.screening.domain.aggregate.Screening;
import com.movie.shop.api.screening.domain.aggregate.ScreeningStatus;
import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;
import com.movie.shop.api.screening.domain.policy.status.AuditoriumScreeningAvailability;
import com.movie.shop.api.screening.domain.policy.status.MovieSchedulingAvailability;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScreeningConflictValidationPolicyTest {

    private static final int MOVIE_RUNTIME_MINUTES = 120;

    private final long movieId = 1L;
    private final long theaterId = 2L;
    private OffsetDateTime screeningStart;
    private OffsetDateTime screeningEnd;

    @BeforeEach
    void setUp() {
        screeningStart = OffsetDateTime.parse("2026-02-10T10:00:00Z");
        screeningEnd = OffsetDateTime.parse("2026-02-10T12:00:00Z");
    }

    @Test
    @DisplayName("후보 상영이 없으면 충돌 검증에 성공한다")
    void validateNoConflict_withNoCandidates_succeeds() {
        ScreeningConflictValidationPolicy policy = new ScreeningConflictValidationPolicy(List.of());

        assertThatCode(() -> policy.validateNoConflict(screeningStart, screeningEnd))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("후보가 모두 CANCELED 상태이면 충돌 검증에 성공한다")
    void validateNoConflict_withOnlyCanceledCandidates_succeeds() {
        Screening canceledScreening = createScreening(ScreeningStatus.CANCELED);
        ScreeningConflictValidationPolicy policy = new ScreeningConflictValidationPolicy(List.of(canceledScreening));

        assertThatCode(() -> policy.validateNoConflict(screeningStart, screeningEnd))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("후보 중 하나라도 충돌하면 충돌 검증 시 예외가 발생한다")
    void validateNoConflict_withConflict_throwsException() {
        Screening scheduledScreening = createScreening(ScreeningStatus.SCHEDULED);
        ScreeningConflictValidationPolicy policy = new ScreeningConflictValidationPolicy(List.of(scheduledScreening));

        assertThatThrownBy(() -> policy.validateNoConflict(screeningStart, screeningEnd))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("동일한 상영관에 상영 시간이 겹치는 일정");
    }

    @Test
    @DisplayName("수정 경로에서 자기 제외 후 남은 후보가 충돌하면 예외가 발생한다")
    void validateNoConflict_withConflictExcludingSelf_throwsException() {
        Screening remainingCandidate = createScreening(ScreeningStatus.SCHEDULED);
        ScreeningConflictValidationPolicy policy = new ScreeningConflictValidationPolicy(List.of(remainingCandidate));

        assertThatThrownBy(() -> policy.validateNoConflict(screeningStart, screeningEnd))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("동일한 상영관에 상영 시간이 겹치는 일정");
    }

    private Screening createScreening(ScreeningStatus status) {
        ScreeningScheduleValidationPolicy schedulePolicy = new ScreeningScheduleValidationPolicy(
                Optional.of(new MovieSchedulingAvailability(true, MOVIE_RUNTIME_MINUTES)),
                Optional.of(new AuditoriumScreeningAvailability(true))
        );
        ScreeningConflictValidationPolicy conflictPolicy = new ScreeningConflictValidationPolicy(List.of());
        ScreeningTimeRuntimeValidationPolicy runtimePolicy = new ScreeningTimeRuntimeValidationPolicy(
                Optional.of(new MovieSchedulingAvailability(true, MOVIE_RUNTIME_MINUTES))
        );

        Screening screening = Screening.register(
                schedulePolicy,
                conflictPolicy,
                runtimePolicy,
                movieId,
                theaterId,
                screeningStart,
                screeningEnd,
                screeningStart.minusDays(1),
                screeningStart
        );

        if (status == ScreeningStatus.CANCELED) {
            screening.cancel("취소 사유", screeningStart.minusHours(1));
        }

        return screening;
    }
}
