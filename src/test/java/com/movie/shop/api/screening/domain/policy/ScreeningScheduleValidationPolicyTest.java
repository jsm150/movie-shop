package com.movie.shop.api.screening.domain.policy;

import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;
import com.movie.shop.api.screening.domain.policy.status.AuditoriumScreeningAvailability;
import com.movie.shop.api.screening.domain.policy.status.MovieSchedulingAvailability;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScreeningScheduleValidationPolicyTest {

    private static final int MOVIE_RUNTIME_MINUTES = 120;

    @Test
    @DisplayName("영화, 상영관 조건이 모두 유효하면 신규 상영 일정 검증에 성공한다")
    void validateCanCreateScreeningSchedule_withValidData_succeeds() {
        ScreeningScheduleValidationPolicy policy = newPolicy(
                Optional.of(movieAvailability(true, MOVIE_RUNTIME_MINUTES)),
                Optional.of(theaterAvailability(true))
        );

        assertThatCode(policy::validate)
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("영화, 상영관 조건이 모두 유효하면 상영 일정 변경 검증에 성공한다")
    void validateCanRescheduleScreening_withValidData_succeeds() {
        ScreeningScheduleValidationPolicy policy = newPolicy(
                Optional.of(movieAvailability(true, MOVIE_RUNTIME_MINUTES)),
                Optional.of(theaterAvailability(true))
        );

        assertThatCode(policy::validate)
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("영화 정보를 찾을 수 없으면 신규 상영 일정 검증 시 예외가 발생한다")
    void validateCanCreateScreeningSchedule_withMissingMovie_throwsException() {
        ScreeningScheduleValidationPolicy policy = newPolicy(
                Optional.empty(),
                Optional.of(theaterAvailability(true))
        );

        assertThatThrownBy(policy::validate)
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("영화 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("상영 불가 상태의 영화면 신규 상영 일정 검증 시 예외가 발생한다")
    void validateCanCreateScreeningSchedule_withUnschedulableMovie_throwsException() {
        ScreeningScheduleValidationPolicy policy = newPolicy(
                Optional.of(movieAvailability(false, MOVIE_RUNTIME_MINUTES)),
                Optional.of(theaterAvailability(true))
        );

        assertThatThrownBy(policy::validate)
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("COMING_SOON 또는 NOW_SHOWING");
    }

    @Test
    @DisplayName("상영관 정보를 찾을 수 없으면 신규 상영 일정 검증 시 예외가 발생한다")
    void validateCanCreateScreeningSchedule_withMissingTheater_throwsException() {
        ScreeningScheduleValidationPolicy policy = newPolicy(
                Optional.of(movieAvailability(true, MOVIE_RUNTIME_MINUTES)),
                Optional.empty()
        );

        assertThatThrownBy(policy::validate)
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("상영관 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("상영 불가 상태의 상영관이면 신규 상영 일정 검증 시 예외가 발생한다")
    void validateCanCreateScreeningSchedule_withUnavailableTheater_throwsException() {
        ScreeningScheduleValidationPolicy policy = newPolicy(
                Optional.of(movieAvailability(true, MOVIE_RUNTIME_MINUTES)),
                Optional.of(theaterAvailability(false))
        );

        assertThatThrownBy(policy::validate)
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("활성화된 상영관에서만");
    }

    private ScreeningScheduleValidationPolicy newPolicy(Optional<MovieSchedulingAvailability> movieAvailability,
                                                        Optional<AuditoriumScreeningAvailability> theaterAvailability) {
        return new ScreeningScheduleValidationPolicy(movieAvailability, theaterAvailability);
    }

    private MovieSchedulingAvailability movieAvailability(boolean schedulable, int runtimeMinutes) {
        return new MovieSchedulingAvailability(schedulable, runtimeMinutes);
    }

    private AuditoriumScreeningAvailability theaterAvailability(boolean available) {
        return new AuditoriumScreeningAvailability(available);
    }
}
