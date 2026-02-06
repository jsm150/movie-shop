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
@DisplayName("DeleteScreeningCommandHandler 통합 테스트")
class DeleteScreeningCommandHandlerIntegrationTest extends ScreeningIntegrationTestSupport {

    @Test
    @Transactional
    @DisplayName("SCHEDULED 상태의 상영은 삭제할 수 있다")
    void deleteScreening_withScheduledStatus_success() {
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

        DeleteScreeningCommand command = new DeleteScreeningCommand(screening.getId());

        // when
        pipeline.send(command);
        flushAndClear();

        // then
        assertThat(screeningJpaPort.findById(screening.getId())).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("존재하지 않는 상영 ID 삭제는 실패한다")
    void deleteScreening_withMissingId_throwsException() {
        // given
        DeleteScreeningCommand command = new DeleteScreeningCommand(999999L);

        // when & then
        assertThatThrownBy(() -> pipeline.send(command))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("상영 정보를 찾을 수 없습니다.");
    }

    @Test
    @Transactional
    @DisplayName("SCHEDULED가 아닌 상영은 삭제할 수 없다")
    void deleteScreening_whenNotScheduled_throwsException() {
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

        DeleteScreeningCommand command = new DeleteScreeningCommand(screening.getId());

        // when & then
        assertThatThrownBy(() -> pipeline.send(command))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("SCHEDULED 상태의 상영만 삭제할 수 있습니다.");

        Screening notDeleted = screeningJpaPort.findById(screening.getId()).orElseThrow();
        assertThat(notDeleted.getStatus()).isEqualTo(ScreeningStatus.ON_SALE);
    }
}
