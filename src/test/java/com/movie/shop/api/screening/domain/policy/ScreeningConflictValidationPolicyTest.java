package com.movie.shop.api.screening.domain.policy;

import com.movie.shop.api.screening.domain.aggregate.Screening;
import com.movie.shop.api.screening.domain.aggregate.ScreeningStatus;
import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;
import com.movie.shop.api.screening.domain.port.LoadScreeningConflictCandidatesPort;
import com.movie.shop.api.screening.domain.port.MemoizedMovieSchedulingAvailabilityPort;
import com.movie.shop.api.screening.domain.policy.status.MovieSchedulingAvailability;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScreeningConflictValidationPolicyTest {

    private static final int MOVIE_RUNTIME_MINUTES = 120;

    private final long movieId = 1L;
    private final long auditoriumId = 2L;
    private final long theaterId = 3L;
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
        ScreeningConflictValidationPolicy policy = new ScreeningConflictValidationPolicy(conflictCandidatesPort(List.of()));

        assertThatCode(() -> policy.validateNoConflict(auditoriumId, screeningStart, screeningEnd))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("후보가 모두 CANCELED 상태이면 충돌 검증에 성공한다")
    void validateNoConflict_withOnlyCanceledCandidates_succeeds() {
        Screening canceledScreening = createScreening(ScreeningStatus.CANCELED);
        ScreeningConflictValidationPolicy policy =
                new ScreeningConflictValidationPolicy(conflictCandidatesPort(List.of(canceledScreening)));

        assertThatCode(() -> policy.validateNoConflict(auditoriumId, screeningStart, screeningEnd))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("후보 중 하나라도 충돌하면 충돌 검증 시 예외가 발생한다")
    void validateNoConflict_withConflict_throwsException() {
        Screening scheduledScreening = createScreening(ScreeningStatus.SCHEDULED);
        ScreeningConflictValidationPolicy policy =
                new ScreeningConflictValidationPolicy(conflictCandidatesPort(List.of(scheduledScreening)));

        assertThatThrownBy(() -> policy.validateNoConflict(auditoriumId, screeningStart, screeningEnd))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("동일한 상영관에 상영 시간이 겹치는 일정");
    }

    @Test
    @DisplayName("수정 경로에서 자기 자신만 후보면 self 제외 후 충돌 검증에 성공한다")
    void validateNoConflictExcludingSelf_withOnlySelf_succeeds() throws Exception {
        Screening self = createScreening(ScreeningStatus.SCHEDULED);
        setId(self, 100L);
        ScreeningConflictValidationPolicy policy =
                new ScreeningConflictValidationPolicy(conflictCandidatesPort(List.of(self)));

        assertThatCode(() -> policy.validateNoConflictExcludingSelf(auditoriumId, 100L, screeningStart, screeningEnd))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("수정 경로에서 자기 제외 후 남은 후보가 충돌하면 예외가 발생한다")
    void validateNoConflictExcludingSelf_withConflictAfterSelfExcluded_throwsException() throws Exception {
        Screening self = createScreening(ScreeningStatus.SCHEDULED);
        setId(self, 100L);
        Screening remainingCandidate = createScreening(ScreeningStatus.SCHEDULED);
        setId(remainingCandidate, 101L);
        ScreeningConflictValidationPolicy policy =
                new ScreeningConflictValidationPolicy(conflictCandidatesPort(List.of(self, remainingCandidate)));

        assertThatThrownBy(() -> policy.validateNoConflictExcludingSelf(auditoriumId, 100L, screeningStart, screeningEnd))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("동일한 상영관에 상영 시간이 겹치는 일정");
    }

    @Test
    @DisplayName("생성 시 충돌 후보 조회 포트가 null이면 예외가 발생한다")
    void constructor_whenConflictCandidatesPortNull_throwsException() {
        assertThatThrownBy(() -> new ScreeningConflictValidationPolicy(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("상영 충돌 후보 조회 포트는 필수입니다.");
    }

    private Screening createScreening(ScreeningStatus status) {
        MemoizedMovieSchedulingAvailabilityPort movieSchedulingAvailabilityPort =
                new MemoizedMovieSchedulingAvailabilityPort(movieId -> java.util.Optional.of(new MovieSchedulingAvailability(true, MOVIE_RUNTIME_MINUTES)));
        ScreeningScheduleValidationPolicy schedulePolicy = new ScreeningScheduleValidationPolicy(
                movieSchedulingAvailabilityPort,
                auditoriumId -> java.util.Optional.of(true)
        );
        ScreeningConflictValidationPolicy conflictPolicy = new ScreeningConflictValidationPolicy(conflictCandidatesPort(List.of()));
        ScreeningTimeRuntimeValidationPolicy runtimePolicy = new ScreeningTimeRuntimeValidationPolicy(
                movieSchedulingAvailabilityPort
        );

        Screening screening = Screening.register(
                schedulePolicy,
                conflictPolicy,
                runtimePolicy,
                movieId,
                auditoriumId,
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

    private LoadScreeningConflictCandidatesPort conflictCandidatesPort(List<Screening> conflictCandidates) {
        return (loadedAuditoriumId, startTime, endTime) -> conflictCandidates;
    }

    private void setId(Screening screening, long id) throws Exception {
        var idField = Screening.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(screening, id);
    }
}
