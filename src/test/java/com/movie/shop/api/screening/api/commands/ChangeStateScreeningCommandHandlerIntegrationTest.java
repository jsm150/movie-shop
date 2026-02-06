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
@DisplayName("ChangeStateScreeningCommandHandler 통합 테스트")
class ChangeStateScreeningCommandHandlerIntegrationTest extends ScreeningIntegrationTestSupport {

    @Test
    @Transactional
    @DisplayName("OPEN_SALES 명령으로 SCHEDULED -> ON_SALE 전이된다")
    void changeState_openSales_success() {
        // given
        Screening screening = createScheduledScreening();
        ChangeStateScreeningCommand command = new ChangeStateScreeningCommand(
                screening.getId(),
                ChangeStateScreeningCommand.ChangeState.OPEN_SALES,
                null
        );

        // when
        pipeline.send(command);
        flushAndClear();

        // then
        Screening updated = screeningJpaPort.findById(screening.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ScreeningStatus.ON_SALE);
    }

    @Test
    @Transactional
    @DisplayName("CLOSE_SALES 명령으로 ON_SALE -> SALES_CLOSED 전이된다")
    void changeState_closeSales_success() {
        // given
        Screening screening = createScheduledScreening();
        pipeline.send(new ChangeStateScreeningCommand(
                screening.getId(),
                ChangeStateScreeningCommand.ChangeState.OPEN_SALES,
                null
        ));
        flushAndClear();

        ChangeStateScreeningCommand command = new ChangeStateScreeningCommand(
                screening.getId(),
                ChangeStateScreeningCommand.ChangeState.CLOSE_SALES,
                null
        );

        // when
        pipeline.send(command);
        flushAndClear();

        // then
        Screening updated = screeningJpaPort.findById(screening.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ScreeningStatus.SALES_CLOSED);
    }

    @Test
    @Transactional
    @DisplayName("FINISH 명령으로 SALES_CLOSED -> FINISHED 전이된다")
    void changeState_finish_success() {
        // given
        Screening screening = createScheduledScreening();
        pipeline.send(new ChangeStateScreeningCommand(
                screening.getId(),
                ChangeStateScreeningCommand.ChangeState.OPEN_SALES,
                null
        ));
        pipeline.send(new ChangeStateScreeningCommand(
                screening.getId(),
                ChangeStateScreeningCommand.ChangeState.CLOSE_SALES,
                null
        ));
        flushAndClear();

        ChangeStateScreeningCommand command = new ChangeStateScreeningCommand(
                screening.getId(),
                ChangeStateScreeningCommand.ChangeState.FINISH,
                null
        );

        // when
        pipeline.send(command);
        flushAndClear();

        // then
        Screening updated = screeningJpaPort.findById(screening.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ScreeningStatus.FINISHED);
    }

    @Test
    @Transactional
    @DisplayName("CANCEL 명령 실행 시 취소 사유/시간과 함께 CANCELED 상태가 된다")
    void changeState_cancel_success() {
        // given
        Screening screening = createScheduledScreening();
        pipeline.send(new ChangeStateScreeningCommand(
                screening.getId(),
                ChangeStateScreeningCommand.ChangeState.OPEN_SALES,
                null
        ));
        flushAndClear();

        ChangeStateScreeningCommand command = new ChangeStateScreeningCommand(
                screening.getId(),
                ChangeStateScreeningCommand.ChangeState.CANCEL,
                "상영 장비 점검"
        );

        // when
        pipeline.send(command);
        flushAndClear();

        // then
        Screening updated = screeningJpaPort.findById(screening.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ScreeningStatus.CANCELED);
        assertThat(updated.getCancelReason()).isEqualTo("상영 장비 점검");
        assertThat(updated.getCanceledAt()).isNotNull();
    }

    @Test
    @Transactional
    @DisplayName("SCHEDULED 상태에서 CLOSE_SALES를 실행하면 실패한다")
    void changeState_invalidTransition_throwsException() {
        // given
        Screening screening = createScheduledScreening();
        ChangeStateScreeningCommand command = new ChangeStateScreeningCommand(
                screening.getId(),
                ChangeStateScreeningCommand.ChangeState.CLOSE_SALES,
                null
        );

        // when & then
        assertThatThrownBy(() -> pipeline.send(command))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("판매 종료는 ON_SALE 상태에서만 가능합니다.");
    }

    @Test
    @Transactional
    @DisplayName("CANCEL 명령에서 공백 취소 사유를 전달하면 실패한다")
    void changeState_cancelWithBlankReason_throwsException() {
        // given
        Screening screening = createScheduledScreening();
        ChangeStateScreeningCommand command = new ChangeStateScreeningCommand(
                screening.getId(),
                ChangeStateScreeningCommand.ChangeState.CANCEL,
                " "
        );

        // when & then
        assertThatThrownBy(() -> pipeline.send(command))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("취소 사유는 필수입니다.");
    }

    @Test
    @Transactional
    @DisplayName("존재하지 않는 상영 ID로 상태 변경 시 실패한다")
    void changeState_withMissingScreeningId_throwsException() {
        // given
        ChangeStateScreeningCommand command = new ChangeStateScreeningCommand(
                999999L,
                ChangeStateScreeningCommand.ChangeState.OPEN_SALES,
                null
        );

        // when & then
        assertThatThrownBy(() -> pipeline.send(command))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("상영 정보를 찾을 수 없습니다.");
    }

    private Screening createScheduledScreening() {
        Movie movie = createMovie(MovieStatus.COMING_SOON);
        Theater theater = createTheater(true);
        return createScreening(
                movie.getId(),
                theater.getId(),
                OffsetDateTime.parse("2026-03-01T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T12:00:00Z"),
                OffsetDateTime.parse("2026-02-20T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T10:00:00Z")
        );
    }
}
