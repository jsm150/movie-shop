package com.movie.shop.api.screening.api.commands;

import com.movie.shop.api.movie.domain.aggregate.Movie;
import com.movie.shop.api.movie.domain.aggregate.MovieStatus;
import com.movie.shop.api.screening.domain.aggregate.Screening;
import com.movie.shop.api.screening.domain.aggregate.ScreeningStatus;
import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;
import com.movie.shop.api.theater.domain.aggregate.Theater;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DisplayName("UpdateScreeningCommandHandler 통합 테스트")
class UpdateScreeningCommandHandlerIntegrationTest extends ScreeningIntegrationTestSupport {

    @Test
    @Transactional
    @DisplayName("SCHEDULED 상태 상영의 시간 수정하면 DB에 반영된다")
    void updateScreening_withScheduledStatus_updatesDatabase() {
        // given
        Movie movie = createMovie(MovieStatus.COMING_SOON);
        Theater theater = createTheater(true);
        Screening screening = createScreening(
                movie.getId(),
                theater.getId(),
                OffsetDateTime.parse("2026-03-01T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T12:00:00Z"),
                OffsetDateTime.parse("2026-02-20T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T10:00:00Z")
        );

        OffsetDateTime newStart = OffsetDateTime.parse("2026-03-01T13:00:00Z");
        OffsetDateTime newEnd = OffsetDateTime.parse("2026-03-01T15:00:00Z");
        OffsetDateTime newSalesStart = OffsetDateTime.parse("2026-02-20T12:00:00Z");
        OffsetDateTime newSalesEnd = newStart;

        UpdateScreeningCommand command = new UpdateScreeningCommand(
                screening.getId(),
                newStart,
                newEnd,
                newSalesStart,
                newSalesEnd
        );

        // when
        Long resultId = pipeline.send(command);
        flushAndClear();

        // then
        Screening updated = screeningJpaPort.findById(screening.getId()).orElseThrow();
        assertThat(resultId).isEqualTo(screening.getId());
        assertThat(updated.getStatus()).isEqualTo(ScreeningStatus.SCHEDULED);
        assertThat(updated.getScreeningTimeRange().getStartTime()).isEqualTo(newStart);
        assertThat(updated.getScreeningTimeRange().getEndTime()).isEqualTo(newEnd);
        assertThat(updated.getSalesTimeRange().getSalesStartAt()).isEqualTo(newSalesStart);
        assertThat(updated.getSalesTimeRange().getSalesEndAt()).isEqualTo(newSalesEnd);
    }

    @Test
    @Transactional
    @DisplayName("존재하지 않는 상영 ID로 수정하면 실패한다")
    void updateScreening_withMissingId_throwsException() {
        // given
        UpdateScreeningCommand command = new UpdateScreeningCommand(
                999999L,
                OffsetDateTime.parse("2026-03-01T13:00:00Z"),
                OffsetDateTime.parse("2026-03-01T15:00:00Z"),
                OffsetDateTime.parse("2026-02-20T12:00:00Z"),
                OffsetDateTime.parse("2026-03-01T13:00:00Z")
        );

        // when & then
        assertThatThrownBy(() -> pipeline.send(command))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("상영 정보를 찾을 수 없습니다.");
    }

    @Test
    @Transactional
    @DisplayName("SCHEDULED가 아닌 상태의 상영은 수정할 수 없다")
    void updateScreening_whenNotScheduled_throwsException() {
        // given
        Movie movie = createMovie(MovieStatus.COMING_SOON);
        Theater theater = createTheater(true);
        Screening screening = createScreening(
                movie.getId(),
                theater.getId(),
                OffsetDateTime.parse("2026-03-01T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T12:00:00Z"),
                OffsetDateTime.parse("2026-02-20T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T10:00:00Z")
        );

        Screening loaded = screeningRepository.getById(screening.getId());
        loaded.openSales();
        flushAndClear();

        UpdateScreeningCommand command = new UpdateScreeningCommand(
                screening.getId(),
                OffsetDateTime.parse("2026-03-01T13:00:00Z"),
                OffsetDateTime.parse("2026-03-01T15:00:00Z"),
                OffsetDateTime.parse("2026-02-20T12:00:00Z"),
                OffsetDateTime.parse("2026-03-01T13:00:00Z")
        );

        // when & then
        assertThatThrownBy(() -> pipeline.send(command))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("SCHEDULED 상태의 상영만 일정 변경이 가능합니다.");
    }

    @Test
    @Transactional
    @DisplayName("다른 상영과 시간이 겹치면 수정에 실패한다")
    void updateScreening_withConflict_throwsException() {
        // given
        Movie movie1 = createMovie(MovieStatus.COMING_SOON);
        Movie movie2 = createMovie(MovieStatus.COMING_SOON);
        Theater theater = createTheater(true);

        Screening target = createScreening(
                movie1.getId(),
                theater.getId(),
                OffsetDateTime.parse("2026-03-01T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T12:00:00Z"),
                OffsetDateTime.parse("2026-02-20T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T10:00:00Z")
        );

        createScreening(
                movie2.getId(),
                theater.getId(),
                OffsetDateTime.parse("2026-03-01T13:00:00Z"),
                OffsetDateTime.parse("2026-03-01T15:00:00Z"),
                OffsetDateTime.parse("2026-02-20T11:00:00Z"),
                OffsetDateTime.parse("2026-03-01T13:00:00Z")
        );

        UpdateScreeningCommand command = new UpdateScreeningCommand(
                target.getId(),
                OffsetDateTime.parse("2026-03-01T14:00:00Z"),
                OffsetDateTime.parse("2026-03-01T16:00:00Z"),
                OffsetDateTime.parse("2026-02-20T12:00:00Z"),
                OffsetDateTime.parse("2026-03-01T14:00:00Z")
        );

        // when & then
        assertThatThrownBy(() -> pipeline.send(command))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("동일한 극장에 상영 시간이 겹치는 일정이 존재합니다.");
    }

    @Test
    @Transactional
    @DisplayName("자기 자신과만 시간이 겹치면 수정에 성공한다")
    void updateScreening_withSelfOverlapOnly_updatesSuccessfully() {
        // given
        Movie movie = createMovie(MovieStatus.COMING_SOON);
        Theater theater = createTheater(true);
        Screening target = createScreening(
                movie.getId(),
                theater.getId(),
                OffsetDateTime.parse("2026-03-01T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T12:00:00Z"),
                OffsetDateTime.parse("2026-02-20T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T10:00:00Z")
        );

        OffsetDateTime newStart = OffsetDateTime.parse("2026-03-01T11:00:00Z");
        OffsetDateTime newEnd = OffsetDateTime.parse("2026-03-01T13:00:00Z");
        OffsetDateTime newSalesStart = OffsetDateTime.parse("2026-02-20T11:00:00Z");
        OffsetDateTime newSalesEnd = newStart;

        UpdateScreeningCommand command = new UpdateScreeningCommand(
                target.getId(),
                newStart,
                newEnd,
                newSalesStart,
                newSalesEnd
        );

        // when
        Long resultId = pipeline.send(command);
        flushAndClear();

        // then
        Screening updated = screeningJpaPort.findById(target.getId()).orElseThrow();
        assertThat(resultId).isEqualTo(target.getId());
        assertThat(updated.getStatus()).isEqualTo(ScreeningStatus.SCHEDULED);
        assertThat(updated.getScreeningTimeRange().getStartTime()).isEqualTo(newStart);
        assertThat(updated.getScreeningTimeRange().getEndTime()).isEqualTo(newEnd);
        assertThat(updated.getSalesTimeRange().getSalesStartAt()).isEqualTo(newSalesStart);
        assertThat(updated.getSalesTimeRange().getSalesEndAt()).isEqualTo(newSalesEnd);
    }

    @Test
    @Transactional
    @DisplayName("연관 영화가 스케줄 불가 상태가 되면 수정에 실패한다")
    void updateScreening_withUnschedulableMovie_throwsException() {
        // given
        Movie movie = createMovie(MovieStatus.COMING_SOON);
        Theater theater = createTheater(true);
        Screening screening = createScreening(
                movie.getId(),
                theater.getId(),
                OffsetDateTime.parse("2026-03-01T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T12:00:00Z"),
                OffsetDateTime.parse("2026-02-20T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T10:00:00Z")
        );

        Movie loadedMovie = movieRepository.getById(movie.getId());
        loadedMovie.startShowing();
        loadedMovie.endShowing();
        flushAndClear();

        UpdateScreeningCommand command = new UpdateScreeningCommand(
                screening.getId(),
                OffsetDateTime.parse("2026-03-01T13:00:00Z"),
                OffsetDateTime.parse("2026-03-01T15:00:00Z"),
                OffsetDateTime.parse("2026-02-20T12:00:00Z"),
                OffsetDateTime.parse("2026-03-01T13:00:00Z")
        );

        // when & then
        assertThatThrownBy(() -> pipeline.send(command))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("상영 등록/수정은 COMING_SOON 또는 NOW_SHOWING 상태의 영화만 가능합니다.");
    }
}
