package com.movie.shop.api.screening.domain.aggregate;

import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;
import com.movie.shop.api.screening.domain.policy.ScreeningScheduleValidationPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScreeningTest {

    @Mock
    private ScreeningScheduleValidationPolicy policy;

    private long movieId;
    private long theaterId;
    private OffsetDateTime screeningStart;
    private OffsetDateTime screeningEnd;
    private OffsetDateTime salesStart;
    private OffsetDateTime salesEnd;

    @BeforeEach
    void setUp() {
        // 공통 테스트 데이터 초기화
        movieId = 1L;
        theaterId = 2L;
        screeningStart = OffsetDateTime.parse("2026-02-10T10:00:00Z");
        screeningEnd = OffsetDateTime.parse("2026-02-10T12:00:00Z");
        salesStart = OffsetDateTime.parse("2026-02-01T10:00:00Z");
        salesEnd = screeningStart;
    }

    @Test
    void register_withValidPolicy_succeeds() {
        // 실행: 유효한 정책과 일정으로 상영 등록
        Screening screening = Screening.register(
                policy,
                movieId,
                theaterId,
                screeningStart,
                screeningEnd,
                salesStart,
                salesEnd
        );

        // 검증: 정책 호출 및 생성된 상영 정보 확인
        verify(policy).validateCanCreateScreeningSchedule(movieId, theaterId, screeningStart, screeningEnd);
        assertThat(screening.getMovieId()).isEqualTo(movieId);
        assertThat(screening.getTheaterId()).isEqualTo(theaterId);
        assertThat(screening.getStatus()).isEqualTo(ScreeningStatus.SCHEDULED);
        assertThat(screening.getScreeningTimeRange().getStartTime()).isEqualTo(screeningStart);
        assertThat(screening.getScreeningTimeRange().getEndTime()).isEqualTo(screeningEnd);
        assertThat(screening.getSalesTimeRange().getSalesStartAt()).isEqualTo(salesStart);
        assertThat(screening.getSalesTimeRange().getSalesEndAt()).isEqualTo(salesEnd);
    }

    @Test
    void register_withNullPolicy_throwsException() {
        // 검증: 정책이 null이면 상영 등록 실패
        assertThatThrownBy(() -> Screening.register(
                null,
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
    void reschedule_withNullPolicy_throwsException() {
        // 준비: 등록된 상영 생성
        Screening screening = Screening.register(
                policy,
                movieId,
                theaterId,
                screeningStart,
                screeningEnd,
                salesStart,
                salesEnd
        );

        // 검증: 정책이 null이면 일정 변경 실패
        assertThatThrownBy(() -> screening.reschedule(
                null,
                screeningStart.plusHours(1),
                screeningEnd.plusHours(1),
                salesStart.plusDays(1),
                screeningStart.plusHours(1)
        ))
                .isInstanceOf(ScreeningDomainException.class);
    }

    @Test
    void reschedule_whenNotScheduledStatus_throwsException() throws Exception {
        // 준비: 상영을 등록하고 상태를 SCHEDULED 이외로 변경
        Screening screening = Screening.register(
                policy,
                movieId,
                theaterId,
                screeningStart,
                screeningEnd,
                salesStart,
                salesEnd
        );
        setId(screening, 100L);
        screening.openSales();

        OffsetDateTime newStart = screeningStart.plusHours(1);
        OffsetDateTime newEnd = screeningEnd.plusHours(1);
        OffsetDateTime newSalesStart = salesStart.plusHours(1);
        OffsetDateTime newSalesEnd = newStart;

        // 검증: 일정 변경은 실패하지만 정책 검증은 호출됨
        assertThatThrownBy(() -> screening.reschedule(
                policy,
                newStart,
                newEnd,
                newSalesStart,
                newSalesEnd
        ))
                .isInstanceOf(ScreeningDomainException.class);

        verify(policy).validateCanRescheduleScreening(100L, movieId, theaterId, newStart, newEnd);
    }

    @Test
    void reschedule_withValidPolicyAndScheduledStatus_succeeds() throws Exception {
        // 준비: SCHEDULED 상태의 상영 생성
        Screening screening = Screening.register(
                policy,
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

        // 실행: 일정 및 판매 시간 재조정
        screening.reschedule(
                policy,
                newStart,
                newEnd,
                newSalesStart,
                newSalesEnd
        );

        // 검증: 정책 호출 및 변경된 시간 반영 확인
        verify(policy).validateCanRescheduleScreening(101L, movieId, theaterId, newStart, newEnd);
        assertThat(screening.getScreeningTimeRange().getStartTime()).isEqualTo(newStart);
        assertThat(screening.getScreeningTimeRange().getEndTime()).isEqualTo(newEnd);
        assertThat(screening.getSalesTimeRange().getSalesStartAt()).isEqualTo(newSalesStart);
        assertThat(screening.getSalesTimeRange().getSalesEndAt()).isEqualTo(newSalesEnd);
    }
    @Test
    void validateCanRemove_whenScheduled_doesNotThrow() {
        Screening screening = Screening.register(
                policy,
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
    void validateCanRemove_whenNotScheduled_throwsException() {
        Screening screening = Screening.register(
                policy,
                movieId,
                theaterId,
                screeningStart,
                screeningEnd,
                salesStart,
                salesEnd
        );
        screening.openSales();

        assertThatThrownBy(screening::validateCanRemove)
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("SCHEDULED 상태의 상영만 삭제할 수 있습니다.");
    }
    private void setId(Screening screening, long id) throws Exception {
        // 리플렉션으로 식별자를 주입해 상태 전이 시나리오를 검증
        Field idField = Screening.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(screening, id);
    }
}
