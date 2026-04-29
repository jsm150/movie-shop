package com.movie.shop.api.screening.domain.aggregate;

import com.movie.shop.api.screening.domain.condition.AuditoriumScreeningCondition;
import com.movie.shop.api.screening.domain.condition.MovieSchedulingCondition;
import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SalesTimeRangeTest {

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
    @DisplayName("후보 상영이 없으면 판매 시간 범위 생성에 성공한다")
    void create_withNoCandidates_succeeds() {
        assertThatCode(() -> createSalesTimeRange(List.of(), null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("후보가 모두 CANCELED 상태이면 판매 시간 범위 생성에 성공한다")
    void create_withOnlyCanceledCandidates_succeeds() {
        Screening canceledScreening = createScreening(ScreeningStatus.CANCELED);

        assertThatCode(() -> createSalesTimeRange(List.of(canceledScreening), null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("후보 중 하나라도 충돌하면 판매 시간 범위 생성 시 예외가 발생한다")
    void create_withConflict_throwsException() {
        Screening scheduledScreening = createScreening(ScreeningStatus.SCHEDULED);

        assertThatThrownBy(() -> createSalesTimeRange(List.of(scheduledScreening), null))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("동일한 상영관에 상영 시간이 겹치는 일정");
    }

    @Test
    @DisplayName("수정 경로에서 자기 자신만 후보면 self 제외 후 판매 시간 범위 생성에 성공한다")
    void createForReschedule_withOnlySelf_succeeds() throws Exception {
        Screening self = createScreening(ScreeningStatus.SCHEDULED);
        setId(self, 100L);

        assertThatCode(() -> createSalesTimeRange(List.of(self), 100L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("수정 경로에서 자기 제외 후 남은 후보가 충돌하면 예외가 발생한다")
    void createForReschedule_withConflictAfterSelfExcluded_throwsException() throws Exception {
        Screening self = createScreening(ScreeningStatus.SCHEDULED);
        setId(self, 100L);
        Screening remainingCandidate = createScreening(ScreeningStatus.SCHEDULED);
        setId(remainingCandidate, 101L);

        assertThatThrownBy(() -> createSalesTimeRange(List.of(self, remainingCandidate), 100L))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("동일한 상영관에 상영 시간이 겹치는 일정");
    }

    @Test
    @DisplayName("충돌 후보가 null이면 생성에 실패한다")
    void create_whenConflictCandidatesNull_fails() {
        var validation = SalesTimeRange.create(
                screeningStart.minusDays(1),
                screeningStart,
                auditoriumId,
                null,
                screeningStart,
                screeningEnd,
                null
        );

        assertThat(validation.isInvalid()).isTrue();
        assertThat(validation.getError().mkString(","))
                .contains("상영 충돌 후보는 필수입니다.");
    }

    private void createSalesTimeRange(List<Screening> overlapCandidates, Long selfScreeningId) {
        SalesTimeRange.create(
                screeningStart.minusDays(1),
                screeningStart,
                auditoriumId,
                selfScreeningId,
                screeningStart,
                screeningEnd,
                overlapCandidates
        ).get();
    }

    private Screening createScreening(ScreeningStatus status) {
        Screening screening = Screening.register(
                movieId,
                auditoriumId,
                Optional.of(new MovieSchedulingCondition(true, MOVIE_RUNTIME_MINUTES)),
                Optional.of(new AuditoriumScreeningCondition(theaterId, true)),
                List.of(),
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

    private void setId(Screening screening, long id) throws Exception {
        var idField = Screening.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(screening, id);
    }
}
