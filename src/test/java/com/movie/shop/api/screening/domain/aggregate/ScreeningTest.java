package com.movie.shop.api.screening.domain.aggregate;

import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;
import com.movie.shop.api.screening.domain.policy.ScreeningScheduleValidationPolicy;
import com.movie.shop.api.screening.domain.policy.ScreeningTimeRuntimeValidationPolicy;
import com.movie.shop.api.screening.domain.policy.MovieSchedulingAvailability;
import com.movie.shop.api.screening.domain.policy.TheaterScreeningAvailability;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScreeningTest {

    private ScreeningScheduleValidationPolicy schedulePolicy;
    private ScreeningTimeRuntimeValidationPolicy runtimePolicy;

    private long movieId;
    private long theaterId;
    private OffsetDateTime screeningStart;
    private OffsetDateTime screeningEnd;
    private OffsetDateTime salesStart;
    private OffsetDateTime salesEnd;

    @BeforeEach
    void setUp() {
        movieId = 1L;
        theaterId = 2L;
        screeningStart = OffsetDateTime.parse("2026-02-10T10:00:00Z");
        screeningEnd = OffsetDateTime.parse("2026-02-10T12:00:00Z");
        salesStart = OffsetDateTime.parse("2026-02-01T10:00:00Z");
        salesEnd = screeningStart;
        schedulePolicy = new ScreeningScheduleValidationPolicy(
                Optional.of(new MovieSchedulingAvailability(true, 120)),
                Optional.of(new TheaterScreeningAvailability(true)),
                List.of()
        );
        runtimePolicy = new ScreeningTimeRuntimeValidationPolicy(
                Optional.of(new MovieSchedulingAvailability(true, 120))
        );
    }

    @Test
    @DisplayName("유효한 정책으로 상영을 등록하면 SCHEDULED 상태로 생성된다")
    void register_withValidPolicy_succeeds() {
        Screening screening = Screening.register(
                schedulePolicy,
                runtimePolicy,
                movieId,
                theaterId,
                screeningStart,
                screeningEnd,
                salesStart,
                salesEnd
        );

        assertThat(screening.getMovieId()).isEqualTo(movieId);
        assertThat(screening.getTheaterId()).isEqualTo(theaterId);
        assertThat(screening.getStatus()).isEqualTo(ScreeningStatus.SCHEDULED);
        assertThat(screening.getScreeningTimeRange().getStartTime()).isEqualTo(screeningStart);
        assertThat(screening.getScreeningTimeRange().getEndTime()).isEqualTo(screeningEnd);
        assertThat(screening.getSalesTimeRange().getSalesStartAt()).isEqualTo(salesStart);
        assertThat(screening.getSalesTimeRange().getSalesEndAt()).isEqualTo(salesEnd);
    }

    @Test
    @DisplayName("정책이 null이면 상영 등록 시 예외가 발생한다")
    void register_withNullPolicy_throwsException() {
        assertThatThrownBy(() -> Screening.register(
                null,
                runtimePolicy,
                movieId,
                theaterId,
                screeningStart,
                screeningEnd,
                salesStart,
                salesEnd
        ))
                .isInstanceOf(ScreeningDomainException.class);
    }

    @Test
    @DisplayName("정책이 null이면 상영 일정 변경 시 예외가 발생한다")
    void reschedule_withNullPolicy_throwsException() {
        Screening screening = Screening.register(
                schedulePolicy,
                runtimePolicy,
                movieId,
                theaterId,
                screeningStart,
                screeningEnd,
                salesStart,
                salesEnd
        );

        assertThatThrownBy(() -> screening.reschedule(
                null,
                runtimePolicy,
                screeningStart.plusHours(1),
                screeningEnd.plusHours(1),
                salesStart.plusDays(1),
                screeningStart.plusHours(1)
        ))
                .isInstanceOf(ScreeningDomainException.class);
    }

    @Test
    @DisplayName("SCHEDULED가 아닌 상태에서 일정 변경을 요청하면 예외가 발생한다")
    void reschedule_whenNotScheduledStatus_throwsException() throws Exception {
        Screening screening = Screening.register(
                schedulePolicy,
                runtimePolicy,
                movieId,
                theaterId,
                screeningStart,
                screeningEnd,
                salesStart,
                salesEnd
        );
        setId(screening, 100L);
        screening.openSales(OffsetDateTime.parse("2026-02-09T10:00:00Z"));

        OffsetDateTime newStart = screeningStart.plusHours(1);
        OffsetDateTime newEnd = screeningEnd.plusHours(1);
        OffsetDateTime newSalesStart = salesStart.plusHours(1);
        OffsetDateTime newSalesEnd = newStart;

        assertThatThrownBy(() -> screening.reschedule(
                schedulePolicy,
                runtimePolicy,
                newStart,
                newEnd,
                newSalesStart,
                newSalesEnd
        ))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("SCHEDULED 상태의 상영만 일정 변경이 가능합니다.");
    }

    @Test
    @DisplayName("SCHEDULED 상태에서 유효한 정책으로 일정 변경을 요청하면 시간이 변경된다")
    void reschedule_withValidPolicyAndScheduledStatus_succeeds() throws Exception {
        Screening screening = Screening.register(
                schedulePolicy,
                runtimePolicy,
                movieId,
                theaterId,
                screeningStart,
                screeningEnd,
                salesStart,
                salesEnd
        );
        setId(screening, 101L);

        OffsetDateTime newStart = screeningStart.plusHours(2);
        OffsetDateTime newEnd = screeningEnd.plusHours(2);
        OffsetDateTime newSalesStart = salesStart.plusHours(2);
        OffsetDateTime newSalesEnd = newStart;

        screening.reschedule(
                schedulePolicy,
                runtimePolicy,
                newStart,
                newEnd,
                newSalesStart,
                newSalesEnd
        );

        assertThat(screening.getScreeningTimeRange().getStartTime()).isEqualTo(newStart);
        assertThat(screening.getScreeningTimeRange().getEndTime()).isEqualTo(newEnd);
        assertThat(screening.getSalesTimeRange().getSalesStartAt()).isEqualTo(newSalesStart);
        assertThat(screening.getSalesTimeRange().getSalesEndAt()).isEqualTo(newSalesEnd);
    }

    @Test
    @DisplayName("SCHEDULED 상태에서 삭제 가능 여부를 검증하면 예외가 발생하지 않는다")
    void validateCanRemove_whenScheduled_doesNotThrow() {
        Screening screening = Screening.register(
                schedulePolicy,
                runtimePolicy,
                movieId,
                theaterId,
                screeningStart,
                screeningEnd,
                salesStart,
                salesEnd
        );

        assertThatCode(screening::validateCanRemove).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("판매 시작 시간 이전에는 OPEN_SALES를 수행할 수 없다")
    void openSales_beforeSalesStart_throwsException() {
        Screening screening = Screening.register(
                schedulePolicy,
                runtimePolicy,
                movieId,
                theaterId,
                screeningStart,
                screeningEnd,
                salesStart,
                salesEnd
        );

        assertThatThrownBy(() -> screening.openSales(OffsetDateTime.parse("2026-01-31T10:00:00Z")))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("판매 시작 시간 이전에는 판매를 시작할 수 없습니다.");
    }

    @Test
    @DisplayName("판매 시작 시간과 같거나 이후면 OPEN_SALES를 수행할 수 있다")
    void openSales_atOrAfterSalesStart_succeeds() {
        Screening screening = Screening.register(
                schedulePolicy,
                runtimePolicy,
                movieId,
                theaterId,
                screeningStart,
                screeningEnd,
                salesStart,
                salesEnd
        );

        screening.openSales(salesStart);

        assertThat(screening.getStatus()).isEqualTo(ScreeningStatus.ON_SALE);
    }

    @Test
    @DisplayName("상영 종료 시간 이전에는 FINISH를 수행할 수 없다")
    void finish_beforeScreeningEnd_throwsException() {
        Screening screening = Screening.register(
                schedulePolicy,
                runtimePolicy,
                movieId,
                theaterId,
                screeningStart,
                screeningEnd,
                salesStart,
                salesEnd
        );
        screening.openSales(OffsetDateTime.parse("2026-02-09T10:00:00Z"));
        screening.closeSales();

        assertThatThrownBy(() -> screening.finish(OffsetDateTime.parse("2026-02-10T11:59:00Z")))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("상영 종료 시간 이전에는 상영을 종료할 수 없습니다.");
    }

    @Test
    @DisplayName("상영 종료 시간과 같거나 이후면 FINISH를 수행할 수 있다")
    void finish_atOrAfterScreeningEnd_succeeds() {
        Screening screening = Screening.register(
                schedulePolicy,
                runtimePolicy,
                movieId,
                theaterId,
                screeningStart,
                screeningEnd,
                salesStart,
                salesEnd
        );
        screening.openSales(OffsetDateTime.parse("2026-02-09T10:00:00Z"));
        screening.closeSales();

        screening.finish(screeningEnd);

        assertThat(screening.getStatus()).isEqualTo(ScreeningStatus.FINISHED);
    }

    @Test
    @DisplayName("SCHEDULED가 아닌 상태에서 삭제 가능 여부를 검증하면 예외가 발생한다")
    void validateCanRemove_whenNotScheduled_throwsException() {
        Screening screening = Screening.register(
                schedulePolicy,
                runtimePolicy,
                movieId,
                theaterId,
                screeningStart,
                screeningEnd,
                salesStart,
                salesEnd
        );
        screening.openSales(OffsetDateTime.parse("2026-02-09T10:00:00Z"));

        assertThatThrownBy(screening::validateCanRemove)
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("SCHEDULED 상태의 상영만 삭제할 수 있습니다.");
    }

    @Test
    @DisplayName("SCHEDULED 상태이면 극장 비활성화 또는 삭제 차단 여부가 true를 반환한다")
    void blocksTheaterDeactivationOrDeletion_whenScheduled_returnsTrue() {
        Screening screening = Screening.register(
                schedulePolicy,
                runtimePolicy,
                movieId,
                theaterId,
                screeningStart,
                screeningEnd,
                salesStart,
                salesEnd
        );

        assertThat(screening.blocksTheaterDeactivationOrDeletion()).isTrue();
    }

    @Test
    @DisplayName("ON_SALE 상태이면 극장 비활성화 또는 삭제 차단 여부가 true를 반환한다")
    void blocksTheaterDeactivationOrDeletion_whenOnSale_returnsTrue() {
        Screening screening = Screening.register(
                schedulePolicy,
                runtimePolicy,
                movieId,
                theaterId,
                screeningStart,
                screeningEnd,
                salesStart,
                salesEnd
        );
        screening.openSales(OffsetDateTime.parse("2026-02-09T10:00:00Z"));

        assertThat(screening.blocksTheaterDeactivationOrDeletion()).isTrue();
    }

    @Test
    @DisplayName("SALES_CLOSED 상태이면 극장 비활성화 또는 삭제 차단 여부가 true를 반환한다")
    void blocksTheaterDeactivationOrDeletion_whenSalesClosed_returnsTrue() {
        Screening screening = Screening.register(
                schedulePolicy,
                runtimePolicy,
                movieId,
                theaterId,
                screeningStart,
                screeningEnd,
                salesStart,
                salesEnd
        );
        screening.openSales(OffsetDateTime.parse("2026-02-09T10:00:00Z"));
        screening.closeSales();

        assertThat(screening.blocksTheaterDeactivationOrDeletion()).isTrue();
    }

    @Test
    @DisplayName("CANCELED 상태이면 극장 비활성화 또는 삭제 차단 여부가 false를 반환한다")
    void blocksTheaterDeactivationOrDeletion_whenCanceled_returnsFalse() {
        Screening screening = Screening.register(
                schedulePolicy,
                runtimePolicy,
                movieId,
                theaterId,
                screeningStart,
                screeningEnd,
                salesStart,
                salesEnd
        );
        screening.cancel("취소 사유", OffsetDateTime.parse("2026-02-01T11:00:00Z"));

        assertThat(screening.blocksTheaterDeactivationOrDeletion()).isFalse();
    }

    @Test
    @DisplayName("FINISHED 상태이면 극장 비활성화 또는 삭제 차단 여부가 false를 반환한다")
    void blocksTheaterDeactivationOrDeletion_whenFinished_returnsFalse() {
        Screening screening = Screening.register(
                schedulePolicy,
                runtimePolicy,
                movieId,
                theaterId,
                screeningStart,
                screeningEnd,
                salesStart,
                salesEnd
        );
        screening.openSales(OffsetDateTime.parse("2026-02-09T10:00:00Z"));
        screening.closeSales();
        screening.finish(OffsetDateTime.parse("2026-02-10T12:01:00Z"));

        assertThat(screening.blocksTheaterDeactivationOrDeletion()).isFalse();
    }

    @Test
    @DisplayName("SCHEDULED 상태에서 시간이 겹치면 충돌로 판단한다")
    void hasTimeConflictWith_whenScheduledAndOverlaps_returnsTrue() {
        Screening screening = Screening.register(
                schedulePolicy,
                runtimePolicy,
                movieId,
                theaterId,
                screeningStart,
                screeningEnd,
                salesStart,
                salesEnd
        );

        boolean result = screening.hasTimeConflictWith(
                OffsetDateTime.parse("2026-02-10T11:00:00Z"),
                OffsetDateTime.parse("2026-02-10T13:00:00Z")
        );

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("SCHEDULED 상태에서 시간이 겹치지 않으면 충돌이 아니다")
    void hasTimeConflictWith_whenScheduledAndNotOverlaps_returnsFalse() {
        Screening screening = Screening.register(
                schedulePolicy,
                runtimePolicy,
                movieId,
                theaterId,
                screeningStart,
                screeningEnd,
                salesStart,
                salesEnd
        );

        boolean result = screening.hasTimeConflictWith(
                OffsetDateTime.parse("2026-02-10T12:00:00Z"),
                OffsetDateTime.parse("2026-02-10T13:00:00Z")
        );

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("CANCELED 상태에서는 시간이 겹쳐도 충돌이 아니다")
    void hasTimeConflictWith_whenCanceled_returnsFalse() {
        Screening screening = Screening.register(
                schedulePolicy,
                runtimePolicy,
                movieId,
                theaterId,
                screeningStart,
                screeningEnd,
                salesStart,
                salesEnd
        );
        screening.cancel("취소 사유", OffsetDateTime.parse("2026-02-01T11:00:00Z"));

        boolean result = screening.hasTimeConflictWith(
                OffsetDateTime.parse("2026-02-10T11:00:00Z"),
                OffsetDateTime.parse("2026-02-10T13:00:00Z")
        );

        assertThat(result).isFalse();
    }

    private void setId(Screening screening, long id) throws Exception {
        Field idField = Screening.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(screening, id);
    }
}
