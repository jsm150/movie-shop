package com.movie.shop.api.screening.domain.policy;

import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;
import com.movie.shop.api.screening.domain.policy.port.CheckScreeningTimeConflictPort;
import com.movie.shop.api.screening.domain.policy.port.LoadMovieSchedulingAvailabilityPort;
import com.movie.shop.api.screening.domain.policy.port.LoadTheaterScreeningAvailabilityPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

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
    private CheckScreeningTimeConflictPort checkScreeningTimeConflictPort;

    private ScreeningScheduleValidationPolicy policy;

    private final long screeningId = 10L;
    private final long movieId = 1L;
    private final long theaterId = 2L;
    private OffsetDateTime screeningStart;
    private OffsetDateTime screeningEnd;

    @BeforeEach
    void setUp() {
        // 정책 객체와 공통 시간 데이터 초기화
        policy = new ScreeningScheduleValidationPolicy(
                loadMovieSchedulingAvailabilityPort,
                loadTheaterScreeningAvailabilityPort,
                checkScreeningTimeConflictPort
        );
        screeningStart = OffsetDateTime.parse("2026-02-10T10:00:00Z");
        screeningEnd = OffsetDateTime.parse("2026-02-10T12:00:00Z");
    }

    @Test
    @DisplayName("영화, 상영관, 시간 조건이 모두 유효하면 신규 상영 일정 검증에 성공한다")
    void validateCanCreateScreeningSchedule_withValidData_succeeds() {
        // 준비: 영화/상영관/시간 충돌 조건을 모두 통과하도록 스텁 설정
        when(loadMovieSchedulingAvailabilityPort.loadMovieSchedulingAvailability(movieId)).thenReturn(Optional.of(true));
        when(loadTheaterScreeningAvailabilityPort.loadTheaterScreeningAvailability(theaterId)).thenReturn(Optional.of(true));
        when(checkScreeningTimeConflictPort.hasConflict(theaterId, screeningStart, screeningEnd)).thenReturn(false);

        // 실행: 신규 상영 스케줄 검증
        policy.validateCanCreateScreeningSchedule(movieId, theaterId, screeningStart, screeningEnd);

        // 검증: 시간 충돌 검사 호출 확인
        verify(checkScreeningTimeConflictPort).hasConflict(theaterId, screeningStart, screeningEnd);
    }

    @Test
    @DisplayName("영화, 상영관, 시간 조건이 모두 유효하면 상영 일정 변경 검증에 성공한다")
    void validateCanRescheduleScreening_withValidData_succeeds() {
        // 준비: 재조정 가능한 조건과 충돌 없음 스텁 설정
        when(loadMovieSchedulingAvailabilityPort.loadMovieSchedulingAvailability(movieId)).thenReturn(Optional.of(true));
        when(loadTheaterScreeningAvailabilityPort.loadTheaterScreeningAvailability(theaterId)).thenReturn(Optional.of(true));
        when(checkScreeningTimeConflictPort.hasConflictExcluding(screeningId, theaterId, screeningStart, screeningEnd)).thenReturn(false);

        // 실행: 기존 상영 재조정 검증
        policy.validateCanRescheduleScreening(screeningId, movieId, theaterId, screeningStart, screeningEnd);

        // 검증: 자기 자신을 제외한 충돌 검사 호출 확인
        verify(checkScreeningTimeConflictPort).hasConflictExcluding(screeningId, theaterId, screeningStart, screeningEnd);
    }

    @Test
    @DisplayName("영화 정보를 찾을 수 없으면 신규 상영 일정 검증 시 예외가 발생한다")
    void validateCanCreateScreeningSchedule_withMissingMovie_throwsException() {
        // 준비: 영화 조회 결과가 없는 상황
        when(loadMovieSchedulingAvailabilityPort.loadMovieSchedulingAvailability(movieId)).thenReturn(Optional.empty());

        // 검증: 영화가 없으면 예외 발생, 이후 포트는 호출되지 않음
        assertThatThrownBy(() -> policy.validateCanCreateScreeningSchedule(movieId, theaterId, screeningStart, screeningEnd))
                .isInstanceOf(ScreeningDomainException.class);

        verifyNoInteractions(loadTheaterScreeningAvailabilityPort, checkScreeningTimeConflictPort);
    }

    @Test
    @DisplayName("상영 불가 상태의 영화면 신규 상영 일정 검증 시 예외가 발생한다")
    void validateCanCreateScreeningSchedule_withUnschedulableMovie_throwsException() {
        // 준비: 영화는 존재하지만 스케줄링 불가 상태
        when(loadMovieSchedulingAvailabilityPort.loadMovieSchedulingAvailability(movieId)).thenReturn(Optional.of(false));

        // 검증: 영화가 상영 불가이면 예외 발생, 이후 포트는 호출되지 않음
        assertThatThrownBy(() -> policy.validateCanCreateScreeningSchedule(movieId, theaterId, screeningStart, screeningEnd))
                .isInstanceOf(ScreeningDomainException.class);

        verifyNoInteractions(loadTheaterScreeningAvailabilityPort, checkScreeningTimeConflictPort);
    }

    @Test
    @DisplayName("상영관 정보를 찾을 수 없으면 신규 상영 일정 검증 시 예외가 발생한다")
    void validateCanCreateScreeningSchedule_withMissingTheater_throwsException() {
        // 준비: 영화는 가능하지만 상영관 조회 결과가 없는 상황
        when(loadMovieSchedulingAvailabilityPort.loadMovieSchedulingAvailability(movieId)).thenReturn(Optional.of(true));
        when(loadTheaterScreeningAvailabilityPort.loadTheaterScreeningAvailability(theaterId)).thenReturn(Optional.empty());

        // 검증: 상영관이 없으면 예외 발생, 충돌 검사는 수행되지 않음
        assertThatThrownBy(() -> policy.validateCanCreateScreeningSchedule(movieId, theaterId, screeningStart, screeningEnd))
                .isInstanceOf(ScreeningDomainException.class);

        verifyNoInteractions(checkScreeningTimeConflictPort);
    }

    @Test
    @DisplayName("상영 불가 상태의 상영관이면 신규 상영 일정 검증 시 예외가 발생한다")
    void validateCanCreateScreeningSchedule_withUnavailableTheater_throwsException() {
        // 준비: 상영관이 존재하지만 상영 불가 상태
        when(loadMovieSchedulingAvailabilityPort.loadMovieSchedulingAvailability(movieId)).thenReturn(Optional.of(true));
        when(loadTheaterScreeningAvailabilityPort.loadTheaterScreeningAvailability(theaterId)).thenReturn(Optional.of(false));

        // 검증: 상영관이 비활성/사용불가이면 예외 발생
        assertThatThrownBy(() -> policy.validateCanCreateScreeningSchedule(movieId, theaterId, screeningStart, screeningEnd))
                .isInstanceOf(ScreeningDomainException.class);

        verifyNoInteractions(checkScreeningTimeConflictPort);
    }

    @Test
    @DisplayName("시간 충돌이 있으면 신규 상영 일정 검증 시 예외가 발생한다")
    void validateCanCreateScreeningSchedule_withConflict_throwsException() {
        // 준비: 모든 조건은 통과하지만 시간 충돌이 존재
        when(loadMovieSchedulingAvailabilityPort.loadMovieSchedulingAvailability(movieId)).thenReturn(Optional.of(true));
        when(loadTheaterScreeningAvailabilityPort.loadTheaterScreeningAvailability(theaterId)).thenReturn(Optional.of(true));
        when(checkScreeningTimeConflictPort.hasConflict(theaterId, screeningStart, screeningEnd)).thenReturn(true);

        // 검증: 시간 충돌 시 예외 발생
        assertThatThrownBy(() -> policy.validateCanCreateScreeningSchedule(movieId, theaterId, screeningStart, screeningEnd))
                .isInstanceOf(ScreeningDomainException.class);
    }

    @Test
    @DisplayName("기존 상영 제외 후에도 시간 충돌이 있으면 상영 일정 변경 검증 시 예외가 발생한다")
    void validateCanRescheduleScreening_withConflictExcluding_throwsException() {
        // 준비: 재조정 검증에서 기존 상영 제외 후에도 충돌이 존재
        when(loadMovieSchedulingAvailabilityPort.loadMovieSchedulingAvailability(movieId)).thenReturn(Optional.of(true));
        when(loadTheaterScreeningAvailabilityPort.loadTheaterScreeningAvailability(theaterId)).thenReturn(Optional.of(true));
        when(checkScreeningTimeConflictPort.hasConflictExcluding(screeningId, theaterId, screeningStart, screeningEnd)).thenReturn(true);

        // 검증: 재조정 시 충돌이 있으면 예외 발생
        assertThatThrownBy(() -> policy.validateCanRescheduleScreening(screeningId, movieId, theaterId, screeningStart, screeningEnd))
                .isInstanceOf(ScreeningDomainException.class);
    }
}
