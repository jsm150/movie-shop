package com.movie.shop.api.screening.domain.policy;

import com.movie.shop.api.screening.domain.aggregate.Screening;
import com.movie.shop.api.screening.domain.aggregate.ScreeningStatus;
import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;
import com.movie.shop.api.screening.domain.port.MovieSchedulingAvailability;
import com.movie.shop.api.screening.domain.port.TheaterScreeningAvailability;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScreeningScheduleValidationPolicyTest {

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
    @DisplayName("영화, 상영관, 충돌 조건이 모두 유효하면 신규 상영 일정 검증에 성공한다")
    void validateCanCreateScreeningSchedule_withValidData_succeeds() {
        ScreeningScheduleValidationPolicy policy = newPolicy(
                Optional.of(movieAvailability(true, MOVIE_RUNTIME_MINUTES)),
                Optional.of(theaterAvailability(true)),
                List.of()
        );

        assertThatCode(() -> policy.validateCanCreateScreeningSchedule(screeningStart, screeningEnd))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("영화, 상영관, 충돌 조건이 모두 유효하면 상영 일정 변경 검증에 성공한다")
    void validateCanRescheduleScreening_withValidData_succeeds() {
        ScreeningScheduleValidationPolicy policy = newPolicy(
                Optional.of(movieAvailability(true, MOVIE_RUNTIME_MINUTES)),
                Optional.of(theaterAvailability(true)),
                List.of()
        );

        assertThatCode(() -> policy.validateCanRescheduleScreening(screeningStart, screeningEnd))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("영화 정보를 찾을 수 없으면 신규 상영 일정 검증 시 예외가 발생한다")
    void validateCanCreateScreeningSchedule_withMissingMovie_throwsException() {
        ScreeningScheduleValidationPolicy policy = newPolicy(
                Optional.empty(),
                Optional.of(theaterAvailability(true)),
                List.of()
        );

        assertThatThrownBy(() -> policy.validateCanCreateScreeningSchedule(screeningStart, screeningEnd))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("영화 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("상영 불가 상태의 영화면 신규 상영 일정 검증 시 예외가 발생한다")
    void validateCanCreateScreeningSchedule_withUnschedulableMovie_throwsException() {
        ScreeningScheduleValidationPolicy policy = newPolicy(
                Optional.of(movieAvailability(false, MOVIE_RUNTIME_MINUTES)),
                Optional.of(theaterAvailability(true)),
                List.of()
        );

        assertThatThrownBy(() -> policy.validateCanCreateScreeningSchedule(screeningStart, screeningEnd))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("COMING_SOON 또는 NOW_SHOWING");
    }

    @Test
    @DisplayName("상영관 정보를 찾을 수 없으면 신규 상영 일정 검증 시 예외가 발생한다")
    void validateCanCreateScreeningSchedule_withMissingTheater_throwsException() {
        ScreeningScheduleValidationPolicy policy = newPolicy(
                Optional.of(movieAvailability(true, MOVIE_RUNTIME_MINUTES)),
                Optional.empty(),
                List.of()
        );

        assertThatThrownBy(() -> policy.validateCanCreateScreeningSchedule(screeningStart, screeningEnd))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("극장 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("상영 불가 상태의 상영관이면 신규 상영 일정 검증 시 예외가 발생한다")
    void validateCanCreateScreeningSchedule_withUnavailableTheater_throwsException() {
        ScreeningScheduleValidationPolicy policy = newPolicy(
                Optional.of(movieAvailability(true, MOVIE_RUNTIME_MINUTES)),
                Optional.of(theaterAvailability(false)),
                List.of()
        );

        assertThatThrownBy(() -> policy.validateCanCreateScreeningSchedule(screeningStart, screeningEnd))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("활성화된 극장에서만");
    }

    @Test
    @DisplayName("후보 상영이 없으면 신규 상영 일정 검증에 성공한다")
    void validateCanCreateScreeningSchedule_withNoCandidates_succeeds() {
        ScreeningScheduleValidationPolicy policy = newPolicy(
                Optional.of(movieAvailability(true, MOVIE_RUNTIME_MINUTES)),
                Optional.of(theaterAvailability(true)),
                List.of()
        );

        assertThatCode(() -> policy.validateCanCreateScreeningSchedule(screeningStart, screeningEnd))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("후보가 모두 CANCELED 상태이면 신규 상영 일정 검증에 성공한다")
    void validateCanCreateScreeningSchedule_withOnlyCanceledCandidates_succeeds() {
        Screening canceledScreening = createScreening(ScreeningStatus.CANCELED);
        ScreeningScheduleValidationPolicy policy = newPolicy(
                Optional.of(movieAvailability(true, MOVIE_RUNTIME_MINUTES)),
                Optional.of(theaterAvailability(true)),
                List.of(canceledScreening)
        );

        assertThatCode(() -> policy.validateCanCreateScreeningSchedule(screeningStart, screeningEnd))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("후보 중 하나라도 충돌하면 신규 상영 일정 검증 시 예외가 발생한다")
    void validateCanCreateScreeningSchedule_withConflict_throwsException() {
        Screening scheduledScreening = createScreening(ScreeningStatus.SCHEDULED);
        ScreeningScheduleValidationPolicy policy = newPolicy(
                Optional.of(movieAvailability(true, MOVIE_RUNTIME_MINUTES)),
                Optional.of(theaterAvailability(true)),
                List.of(scheduledScreening)
        );

        assertThatThrownBy(() -> policy.validateCanCreateScreeningSchedule(screeningStart, screeningEnd))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("동일한 극장에 상영 시간이 겹치는 일정");
    }

    @Test
    @DisplayName("기존 상영 제외 후 후보 중 하나라도 충돌하면 일정 변경 검증 시 예외가 발생한다")
    void validateCanRescheduleScreening_withConflictExcluding_throwsException() {
        Screening scheduledScreening = createScreening(ScreeningStatus.SCHEDULED);
        ScreeningScheduleValidationPolicy policy = newPolicy(
                Optional.of(movieAvailability(true, MOVIE_RUNTIME_MINUTES)),
                Optional.of(theaterAvailability(true)),
                List.of(scheduledScreening)
        );

        assertThatThrownBy(() -> policy.validateCanRescheduleScreening(screeningStart, screeningEnd))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("동일한 극장에 상영 시간이 겹치는 일정");
    }

    private ScreeningScheduleValidationPolicy newPolicy(Optional<MovieSchedulingAvailability> movieAvailability,
                                                        Optional<TheaterScreeningAvailability> theaterAvailability,
                                                        List<Screening> conflictCandidates) {
        return new ScreeningScheduleValidationPolicy(movieAvailability, theaterAvailability, conflictCandidates);
    }

    private MovieSchedulingAvailability movieAvailability(boolean schedulable, int runtimeMinutes) {
        return new MovieSchedulingAvailability(schedulable, runtimeMinutes);
    }

    private TheaterScreeningAvailability theaterAvailability(boolean available) {
        return new TheaterScreeningAvailability(available);
    }

    private Screening createScreening(ScreeningStatus status) {
        ScreeningScheduleValidationPolicy dummyPolicy = newPolicy(
                Optional.of(movieAvailability(true, MOVIE_RUNTIME_MINUTES)),
                Optional.of(theaterAvailability(true)),
                List.of()
        );
        ScreeningTimeRuntimeValidationPolicy runtimePolicy = new ScreeningTimeRuntimeValidationPolicy(
                Optional.of(movieAvailability(true, MOVIE_RUNTIME_MINUTES))
        );
        Screening screening = Screening.register(
                dummyPolicy,
                runtimePolicy,
                movieId,
                theaterId,
                screeningStart,
                screeningEnd,
                screeningStart.minusDays(1),
                screeningStart
        );

        switch (status) {
            case ON_SALE -> screening.openSales(OffsetDateTime.parse("2026-02-09T10:00:00Z"));
            case SALES_CLOSED -> {
                screening.openSales(OffsetDateTime.parse("2026-02-09T10:00:00Z"));
                screening.closeSales();
            }
            case CANCELED -> screening.cancel("취소 사유", screeningStart.minusHours(1));
            case FINISHED -> {
                screening.openSales(OffsetDateTime.parse("2026-02-09T10:00:00Z"));
                screening.closeSales();
                screening.finish(screeningEnd.plusMinutes(1));
            }
            case SCHEDULED -> {
            }
        }

        return screening;
    }
}
