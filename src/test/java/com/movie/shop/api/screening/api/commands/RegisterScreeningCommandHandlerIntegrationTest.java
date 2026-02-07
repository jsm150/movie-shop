package com.movie.shop.api.screening.api.commands;

import com.movie.shop.api.movie.domain.aggregate.Movie;
import com.movie.shop.api.movie.domain.aggregate.MovieStatus;
import com.movie.shop.api.screening.domain.aggregate.Screening;
import com.movie.shop.api.screening.domain.aggregate.ScreeningStateChange;
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
@DisplayName("RegisterScreeningCommandHandler 통합 테스트")
class RegisterScreeningCommandHandlerIntegrationTest extends ScreeningIntegrationTestSupport {

    @Test
    @Transactional
    @DisplayName("유효한 데이터로 상영 등록하면 DB에 저장된다")
    void registerScreening_withValidData_persistsToDatabase() {
        // given
        Movie movie = createMovie(MovieStatus.COMING_SOON);
        Theater theater = createTheater(true);
        OffsetDateTime start = OffsetDateTime.parse("2026-03-01T10:00:00Z");
        OffsetDateTime end = OffsetDateTime.parse("2026-03-01T12:00:00Z");
        OffsetDateTime salesStart = OffsetDateTime.parse("2026-02-20T10:00:00Z");
        OffsetDateTime salesEnd = start;

        RegisterScreeningCommand command = new RegisterScreeningCommand(
                movie.getId(),
                theater.getId(),
                start,
                end,
                salesStart,
                salesEnd
        );

        // when
        Long screeningId = pipeline.send(command);
        flushAndClear();

        // then
        Screening screening = screeningJpaPort.findById(screeningId).orElseThrow();
        assertThat(screening.getMovieId()).isEqualTo(movie.getId());
        assertThat(screening.getTheaterId()).isEqualTo(theater.getId());
        assertThat(screening.getStatus()).isEqualTo(ScreeningStatus.SCHEDULED);
        assertThat(screening.getScreeningTimeRange().getStartTime()).isEqualTo(start);
        assertThat(screening.getScreeningTimeRange().getEndTime()).isEqualTo(end);
        assertThat(screening.getSalesTimeRange().getSalesStartAt()).isEqualTo(salesStart);
        assertThat(screening.getSalesTimeRange().getSalesEndAt()).isEqualTo(salesEnd);
    }

    @Test
    @Transactional
    @DisplayName("존재하지 않는 영화 ID로 등록하면 실패한다")
    void registerScreening_withMissingMovie_throwsException() {
        // given
        Theater theater = createTheater(true);

        RegisterScreeningCommand command = new RegisterScreeningCommand(
                999999L,
                theater.getId(),
                OffsetDateTime.parse("2026-03-01T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T12:00:00Z"),
                OffsetDateTime.parse("2026-02-20T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T10:00:00Z")
        );

        // when & then
        assertThatThrownBy(() -> pipeline.send(command))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("영화 정보를 찾을 수 없습니다.");
    }

    @Test
    @Transactional
    @DisplayName("존재하지 않는 극장 ID로 등록하면 실패한다")
    void registerScreening_withMissingTheater_throwsException() {
        // given
        Movie movie = createMovie(MovieStatus.COMING_SOON);

        RegisterScreeningCommand command = new RegisterScreeningCommand(
                movie.getId(),
                999999L,
                OffsetDateTime.parse("2026-03-01T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T12:00:00Z"),
                OffsetDateTime.parse("2026-02-20T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T10:00:00Z")
        );

        // when & then
        assertThatThrownBy(() -> pipeline.send(command))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("극장 정보를 찾을 수 없습니다.");
    }

    @Test
    @Transactional
    @DisplayName("스케줄 불가 상태(PREPARING) 영화로 등록하면 실패한다")
    void registerScreening_withPreparingMovie_throwsException() {
        // given
        Movie movie = createMovie(MovieStatus.PREPARING);
        Theater theater = createTheater(true);

        RegisterScreeningCommand command = new RegisterScreeningCommand(
                movie.getId(),
                theater.getId(),
                OffsetDateTime.parse("2026-03-01T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T12:00:00Z"),
                OffsetDateTime.parse("2026-02-20T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T10:00:00Z")
        );

        // when & then
        assertThatThrownBy(() -> pipeline.send(command))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("상영 등록/수정은 COMING_SOON 또는 NOW_SHOWING 상태의 영화만 가능합니다.");
    }

    @Test
    @Transactional
    @DisplayName("스케줄 불가 상태(ENDED) 영화로 등록하면 실패한다")
    void registerScreening_withEndedMovie_throwsException() {
        // given
        Movie movie = createMovie(MovieStatus.ENDED);
        Theater theater = createTheater(true);

        RegisterScreeningCommand command = new RegisterScreeningCommand(
                movie.getId(),
                theater.getId(),
                OffsetDateTime.parse("2026-03-01T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T12:00:00Z"),
                OffsetDateTime.parse("2026-02-20T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T10:00:00Z")
        );

        // when & then
        assertThatThrownBy(() -> pipeline.send(command))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("상영 등록/수정은 COMING_SOON 또는 NOW_SHOWING 상태의 영화만 가능합니다.");
    }

    @Test
    @Transactional
    @DisplayName("비활성화된 극장으로 등록하면 실패한다")
    void registerScreening_withInactiveTheater_throwsException() {
        // given
        Movie movie = createMovie(MovieStatus.COMING_SOON);
        Theater theater = createTheater(false);

        RegisterScreeningCommand command = new RegisterScreeningCommand(
                movie.getId(),
                theater.getId(),
                OffsetDateTime.parse("2026-03-01T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T12:00:00Z"),
                OffsetDateTime.parse("2026-02-20T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T10:00:00Z")
        );

        // when & then
        assertThatThrownBy(() -> pipeline.send(command))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("활성화된 극장에서만 상영 등록/수정이 가능합니다.");
    }

    @Test
    @Transactional
    @DisplayName("동일 극장에 상영 시간이 겹치면 등록에 실패한다")
    void registerScreening_withOverlappingTime_throwsException() {
        // given
        Movie movie1 = createMovie(MovieStatus.COMING_SOON);
        Movie movie2 = createMovie(MovieStatus.COMING_SOON);
        Theater theater = createTheater(true);

        createScreening(
                movie1.getId(),
                theater.getId(),
                OffsetDateTime.parse("2026-03-01T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T12:00:00Z"),
                OffsetDateTime.parse("2026-02-20T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T10:00:00Z")
        );

        RegisterScreeningCommand command = new RegisterScreeningCommand(
                movie2.getId(),
                theater.getId(),
                OffsetDateTime.parse("2026-03-01T11:00:00Z"),
                OffsetDateTime.parse("2026-03-01T13:00:00Z"),
                OffsetDateTime.parse("2026-02-20T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T11:00:00Z")
        );

        // when & then
        assertThatThrownBy(() -> pipeline.send(command))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("동일한 극장에 상영 시간이 겹치는 일정이 존재합니다.");
    }

    @Test
    @Transactional
    @DisplayName("취소된 상영과 시간이 겹치면 등록에 성공한다")
    void registerScreening_withCanceledOverlap_succeeds() {
        // given
        Movie movie1 = createMovie(MovieStatus.COMING_SOON);
        Movie movie2 = createMovie(MovieStatus.COMING_SOON);
        Theater theater = createTheater(true);

        Screening canceledScreening = createScreening(
                movie1.getId(),
                theater.getId(),
                OffsetDateTime.parse("2026-03-01T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T12:00:00Z"),
                OffsetDateTime.parse("2026-02-20T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T10:00:00Z")
        );

        pipeline.send(new ChangeStateScreeningCommand(
                canceledScreening.getId(),
                ScreeningStateChange.CANCEL,
                "운영상 취소"
        ));
        flushAndClear();

        RegisterScreeningCommand command = new RegisterScreeningCommand(
                movie2.getId(),
                theater.getId(),
                OffsetDateTime.parse("2026-03-01T11:00:00Z"),
                OffsetDateTime.parse("2026-03-01T13:00:00Z"),
                OffsetDateTime.parse("2026-02-20T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T11:00:00Z")
        );

        // when
        Long screeningId = pipeline.send(command);
        flushAndClear();

        // then
        Screening screening = screeningJpaPort.findById(screeningId).orElseThrow();
        assertThat(screening.getStatus()).isEqualTo(ScreeningStatus.SCHEDULED);
    }

    @Test
    @Transactional
    @DisplayName("상영 시간이 영화 런타임보다 짧으면 등록에 실패한다")
    void registerScreening_withShorterThanMovieRuntime_throwsException() {
        // given
        Movie movie = createMovie(MovieStatus.COMING_SOON);
        Theater theater = createTheater(true);

        RegisterScreeningCommand command = new RegisterScreeningCommand(
                movie.getId(),
                theater.getId(),
                OffsetDateTime.parse("2026-03-01T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T11:40:00Z"),
                OffsetDateTime.parse("2026-02-20T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T10:00:00Z")
        );

        // when & then
        assertThatThrownBy(() -> pipeline.send(command))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("상영 시간은 영화 런타임(120분) 이상이어야 합니다.");
    }

    @Test
    @Transactional
    @DisplayName("상영 시작과 종료가 같으면 런타임 검증보다 기존 범위 검증 메시지가 우선한다")
    void registerScreening_withInvalidScreeningRange_throwsScreeningRangeMessage() {
        // given
        Movie movie = createMovie(MovieStatus.COMING_SOON);
        Theater theater = createTheater(true);

        RegisterScreeningCommand command = new RegisterScreeningCommand(
                movie.getId(),
                theater.getId(),
                OffsetDateTime.parse("2026-03-01T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T10:00:00Z"),
                OffsetDateTime.parse("2026-02-20T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T10:00:00Z")
        );

        // when & then
        assertThatThrownBy(() -> pipeline.send(command))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("상영 시작 시간은 상영 종료 시간 이전 이여야 합니다.");
    }
}
