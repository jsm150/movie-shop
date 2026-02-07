package com.movie.shop.api.screening.domain.policy;

import com.movie.shop.api.screening.domain.aggregate.Screening;
import com.movie.shop.api.screening.domain.aggregate.ScreeningStatus;
import com.movie.shop.api.screening.domain.aggregate.port.ScreeningJpaPort;
import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;
import com.movie.shop.api.screening.domain.policy.port.LoadMovieSchedulingAvailabilityPort;
import com.movie.shop.api.screening.domain.policy.port.LoadTheaterScreeningAvailabilityPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScreeningScheduleValidationPolicyTest {

    @Mock
    private LoadMovieSchedulingAvailabilityPort loadMovieSchedulingAvailabilityPort;

    @Mock
    private LoadTheaterScreeningAvailabilityPort loadTheaterScreeningAvailabilityPort;

    @Mock
    private ScreeningJpaPort screeningJpaPort;

    private ScreeningScheduleValidationPolicy policy;

    private final long screeningId = 10L;
    private final long movieId = 1L;
    private final long theaterId = 2L;
    private OffsetDateTime screeningStart;
    private OffsetDateTime screeningEnd;

    @BeforeEach
    void setUp() {
        policy = new ScreeningScheduleValidationPolicy(
                loadMovieSchedulingAvailabilityPort,
                loadTheaterScreeningAvailabilityPort,
                screeningJpaPort
        );
        screeningStart = OffsetDateTime.parse("2026-02-10T10:00:00Z");
        screeningEnd = OffsetDateTime.parse("2026-02-10T12:00:00Z");
    }

    @Test
    @DisplayName("영화, 상영관, 시간 조건이 모두 유효하면 신규 상영 일정 검증에 성공한다")
    void validateCanCreateScreeningSchedule_withValidData_succeeds() {
        when(loadMovieSchedulingAvailabilityPort.loadMovieSchedulingAvailability(movieId)).thenReturn(Optional.of(true));
        when(loadTheaterScreeningAvailabilityPort.loadTheaterScreeningAvailability(theaterId)).thenReturn(Optional.of(true));
        when(screeningJpaPort.findConflictCandidatesByTheaterId(theaterId, screeningStart, screeningEnd))
                .thenReturn(List.of());

        policy.validateCanCreateScreeningSchedule(movieId, theaterId, screeningStart, screeningEnd);

        verify(screeningJpaPort).findConflictCandidatesByTheaterId(theaterId, screeningStart, screeningEnd);
    }

    @Test
    @DisplayName("영화, 상영관, 시간 조건이 모두 유효하면 상영 일정 변경 검증에 성공한다")
    void validateCanRescheduleScreening_withValidData_succeeds() {
        when(loadMovieSchedulingAvailabilityPort.loadMovieSchedulingAvailability(movieId)).thenReturn(Optional.of(true));
        when(loadTheaterScreeningAvailabilityPort.loadTheaterScreeningAvailability(theaterId)).thenReturn(Optional.of(true));
        when(screeningJpaPort.findConflictCandidatesByTheaterIdAndIdNot(theaterId, screeningStart, screeningEnd, screeningId))
                .thenReturn(List.of());

        policy.validateCanRescheduleScreening(screeningId, movieId, theaterId, screeningStart, screeningEnd);

        verify(screeningJpaPort).findConflictCandidatesByTheaterIdAndIdNot(theaterId, screeningStart, screeningEnd, screeningId);
    }

    @Test
    @DisplayName("영화 정보를 찾을 수 없으면 신규 상영 일정 검증 시 예외가 발생한다")
    void validateCanCreateScreeningSchedule_withMissingMovie_throwsException() {
        when(loadMovieSchedulingAvailabilityPort.loadMovieSchedulingAvailability(movieId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> policy.validateCanCreateScreeningSchedule(movieId, theaterId, screeningStart, screeningEnd))
                .isInstanceOf(ScreeningDomainException.class);

        verifyNoInteractions(loadTheaterScreeningAvailabilityPort, screeningJpaPort);
    }

    @Test
    @DisplayName("상영 불가 상태의 영화면 신규 상영 일정 검증 시 예외가 발생한다")
    void validateCanCreateScreeningSchedule_withUnschedulableMovie_throwsException() {
        when(loadMovieSchedulingAvailabilityPort.loadMovieSchedulingAvailability(movieId)).thenReturn(Optional.of(false));

        assertThatThrownBy(() -> policy.validateCanCreateScreeningSchedule(movieId, theaterId, screeningStart, screeningEnd))
                .isInstanceOf(ScreeningDomainException.class);

        verifyNoInteractions(loadTheaterScreeningAvailabilityPort, screeningJpaPort);
    }

    @Test
    @DisplayName("상영관 정보를 찾을 수 없으면 신규 상영 일정 검증 시 예외가 발생한다")
    void validateCanCreateScreeningSchedule_withMissingTheater_throwsException() {
        when(loadMovieSchedulingAvailabilityPort.loadMovieSchedulingAvailability(movieId)).thenReturn(Optional.of(true));
        when(loadTheaterScreeningAvailabilityPort.loadTheaterScreeningAvailability(theaterId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> policy.validateCanCreateScreeningSchedule(movieId, theaterId, screeningStart, screeningEnd))
                .isInstanceOf(ScreeningDomainException.class);

        verifyNoInteractions(screeningJpaPort);
    }

    @Test
    @DisplayName("상영 불가 상태의 상영관이면 신규 상영 일정 검증 시 예외가 발생한다")
    void validateCanCreateScreeningSchedule_withUnavailableTheater_throwsException() {
        when(loadMovieSchedulingAvailabilityPort.loadMovieSchedulingAvailability(movieId)).thenReturn(Optional.of(true));
        when(loadTheaterScreeningAvailabilityPort.loadTheaterScreeningAvailability(theaterId)).thenReturn(Optional.of(false));

        assertThatThrownBy(() -> policy.validateCanCreateScreeningSchedule(movieId, theaterId, screeningStart, screeningEnd))
                .isInstanceOf(ScreeningDomainException.class);

        verifyNoInteractions(screeningJpaPort);
    }

    @Test
    @DisplayName("후보 상영이 없으면 신규 상영 일정 검증에 성공한다")
    void validateCanCreateScreeningSchedule_withNoCandidates_succeeds() {
        when(loadMovieSchedulingAvailabilityPort.loadMovieSchedulingAvailability(movieId)).thenReturn(Optional.of(true));
        when(loadTheaterScreeningAvailabilityPort.loadTheaterScreeningAvailability(theaterId)).thenReturn(Optional.of(true));
        when(screeningJpaPort.findConflictCandidatesByTheaterId(theaterId, screeningStart, screeningEnd))
                .thenReturn(List.of());

        assertThatCode(() -> policy.validateCanCreateScreeningSchedule(movieId, theaterId, screeningStart, screeningEnd))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("후보가 모두 CANCELED 상태이면 신규 상영 일정 검증에 성공한다")
    void validateCanCreateScreeningSchedule_withOnlyCanceledCandidates_succeeds() {
        Screening canceledScreening = createScreening(ScreeningStatus.CANCELED);

        when(loadMovieSchedulingAvailabilityPort.loadMovieSchedulingAvailability(movieId)).thenReturn(Optional.of(true));
        when(loadTheaterScreeningAvailabilityPort.loadTheaterScreeningAvailability(theaterId)).thenReturn(Optional.of(true));
        when(screeningJpaPort.findConflictCandidatesByTheaterId(theaterId, screeningStart, screeningEnd))
                .thenReturn(List.of(canceledScreening));

        assertThatCode(() -> policy.validateCanCreateScreeningSchedule(movieId, theaterId, screeningStart, screeningEnd))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("후보 중 하나라도 충돌하면 신규 상영 일정 검증 시 예외가 발생한다")
    void validateCanCreateScreeningSchedule_withConflict_throwsException() {
        Screening scheduledScreening = createScreening(ScreeningStatus.SCHEDULED);

        when(loadMovieSchedulingAvailabilityPort.loadMovieSchedulingAvailability(movieId)).thenReturn(Optional.of(true));
        when(loadTheaterScreeningAvailabilityPort.loadTheaterScreeningAvailability(theaterId)).thenReturn(Optional.of(true));
        when(screeningJpaPort.findConflictCandidatesByTheaterId(theaterId, screeningStart, screeningEnd))
                .thenReturn(List.of(scheduledScreening));

        assertThatThrownBy(() -> policy.validateCanCreateScreeningSchedule(movieId, theaterId, screeningStart, screeningEnd))
                .isInstanceOf(ScreeningDomainException.class);
    }

    @Test
    @DisplayName("기존 상영 제외 후 후보 중 하나라도 충돌하면 일정 변경 검증 시 예외가 발생한다")
    void validateCanRescheduleScreening_withConflictExcluding_throwsException() {
        Screening scheduledScreening = createScreening(ScreeningStatus.SCHEDULED);

        when(loadMovieSchedulingAvailabilityPort.loadMovieSchedulingAvailability(movieId)).thenReturn(Optional.of(true));
        when(loadTheaterScreeningAvailabilityPort.loadTheaterScreeningAvailability(theaterId)).thenReturn(Optional.of(true));
        when(screeningJpaPort.findConflictCandidatesByTheaterIdAndIdNot(theaterId, screeningStart, screeningEnd, screeningId))
                .thenReturn(List.of(scheduledScreening));

        assertThatThrownBy(() -> policy.validateCanRescheduleScreening(screeningId, movieId, theaterId, screeningStart, screeningEnd))
                .isInstanceOf(ScreeningDomainException.class);
    }

    private Screening createScreening(ScreeningStatus status) {
        ScreeningScheduleValidationPolicy dummyPolicy = org.mockito.Mockito.mock(ScreeningScheduleValidationPolicy.class);
        Screening screening = Screening.register(
                dummyPolicy,
                movieId,
                theaterId,
                screeningStart,
                screeningEnd,
                screeningStart.minusDays(1),
                screeningStart
        );

        switch (status) {
            case ON_SALE -> screening.openSales();
            case SALES_CLOSED -> {
                screening.openSales();
                screening.closeSales();
            }
            case CANCELED -> screening.cancel("취소 사유", screeningStart.minusHours(1));
            case FINISHED -> {
                screening.openSales();
                screening.closeSales();
                screening.finish(screeningEnd.plusMinutes(1));
            }
            case SCHEDULED -> {
            }
        }

        return screening;
    }
}
