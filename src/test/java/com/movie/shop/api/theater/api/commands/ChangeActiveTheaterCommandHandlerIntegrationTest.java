package com.movie.shop.api.theater.api.commands;

import an.awesome.pipelinr.Pipeline;
import com.movie.shop.api.configuration.AbstractContainerBase;
import com.movie.shop.api.movie.domain.aggregate.Actor;
import com.movie.shop.api.movie.domain.aggregate.AudienceRating;
import com.movie.shop.api.movie.domain.aggregate.Movie;
import com.movie.shop.api.movie.domain.aggregate.MovieRepository;
import com.movie.shop.api.movie.domain.aggregate.validator.MovieTitleDuplicateValidator;
import com.movie.shop.api.screening.api.commands.ChangeStateScreeningCommand;
import com.movie.shop.api.screening.api.commands.RegisterScreeningCommand;
import com.movie.shop.api.screening.domain.aggregate.ScreeningStateChange;
import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.aggregate.TheaterActiveChange;
import com.movie.shop.api.theater.domain.aggregate.TheaterRepository;
import com.movie.shop.api.theater.domain.aggregate.TheaterType;
import com.movie.shop.api.theater.domain.aggregate.validator.TheaterNameDuplicateValidator;
import com.movie.shop.api.theater.domain.exceptions.TheaterDomainException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DisplayName("ChangeActiveTheaterCommandHandler 통합 테스트")
class ChangeActiveTheaterCommandHandlerIntegrationTest extends AbstractContainerBase {

    private static final AtomicLong SEQUENCE = new AtomicLong(1L);

    @Autowired
    private Pipeline pipeline;

    @Autowired
    private TheaterNameDuplicateValidator validator;

    @Autowired
    private MovieTitleDuplicateValidator movieTitleDuplicateValidator;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private EntityManager entityManager;

    private Theater createAndSaveTheater(String name, boolean active) {
        Theater theater = Theater.Register(
                validator,
                name,
                1,
                TheaterType.Standard,
                List.of("A1", "A2", "A3", "B1", "B2", "B3"),
                2,
                3
        );

        theater = theaterRepository.save(theater);
        entityManager.flush();
        entityManager.clear();

        if (!active) {
            pipeline.send(new ChangeActiveTheaterCommand(theater.getId(), TheaterActiveChange.DEACTIVATE));
            entityManager.flush();
            entityManager.clear();
            theater = theaterRepository.getById(theater.getId());
        }

        return theater;
    }

    private Movie createAndSaveSchedulableMovie() {
        long seq = SEQUENCE.getAndIncrement();

        Movie movie = Movie.Register(
                movieTitleDuplicateValidator,
                "극장활성변경테스트영화-" + seq,
                "테스트 감독",
                List.of("드라마"),
                120,
                AudienceRating.PG12,
                "테스트 시놉시스",
                OffsetDateTime.parse("2026-01-01T00:00:00Z"),
                List.of(new Actor(
                        "테스트 배우-" + seq,
                        OffsetDateTime.parse("1990-01-01T00:00:00Z"),
                        "Korea",
                        "주연"
                ))
        );
        movie.moveToComingSoon();

        movie = movieRepository.save(movie);
        entityManager.flush();
        entityManager.clear();
        return movie;
    }

    private long createScreening(long movieId, long theaterId) {
        long seq = SEQUENCE.getAndIncrement();

        RegisterScreeningCommand command = new RegisterScreeningCommand(
                movieId,
                theaterId,
                OffsetDateTime.parse("2026-03-01T10:00:00Z").plusHours(seq),
                OffsetDateTime.parse("2026-03-01T12:00:00Z").plusHours(seq),
                OffsetDateTime.parse("2026-02-20T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T10:00:00Z").plusHours(seq)
        );

        Long screeningId = pipeline.send(command);
        entityManager.flush();
        entityManager.clear();
        return screeningId;
    }

    @Test
    @Transactional
    @DisplayName("활성화된 극장을 비활성화한다")
    void changeActive_deactivate_success() {
        // given
        Theater theater = createAndSaveTheater("1관", true);
        long theaterId = theater.getId();

        ChangeActiveTheaterCommand command = new ChangeActiveTheaterCommand(
                theaterId,
                TheaterActiveChange.DEACTIVATE
        );

        // when
        pipeline.send(command);
        entityManager.flush();
        entityManager.clear();

        // then
        Theater updated = theaterRepository.getById(theaterId);
        assertThat(updated.isActive()).isFalse();
    }

    @Test
    @Transactional
    @DisplayName("비활성화된 극장을 활성화한다")
    void changeActive_activate_success() {
        // given
        Theater theater = createAndSaveTheater("2관", false);
        long theaterId = theater.getId();

        ChangeActiveTheaterCommand command = new ChangeActiveTheaterCommand(
                theaterId,
                TheaterActiveChange.ACTIVATE
        );

        // when
        pipeline.send(command);
        entityManager.flush();
        entityManager.clear();

        // then
        Theater updated = theaterRepository.getById(theaterId);
        assertThat(updated.isActive()).isTrue();
    }

    @Test
    @Transactional
    @DisplayName("존재하지 않는 극장 ID로 상태 변경하면 예외가 발생한다")
    void changeActive_withNonExistentId_throwsException() {
        // given
        long nonExistentTheaterId = 999999L;

        ChangeActiveTheaterCommand command = new ChangeActiveTheaterCommand(
                nonExistentTheaterId,
                TheaterActiveChange.DEACTIVATE
        );

        // when & then
        assertThatThrownBy(() -> pipeline.send(command))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("상영관 데이터가 존재하지 않습니다.");
    }

    @Test
    @Transactional
    @DisplayName("ON_SALE 상영이 연결된 극장은 비활성화할 수 없다")
    void changeActive_withOnSaleScreening_throwsException() {
        Theater theater = createAndSaveTheater("3관", true);
        Movie movie = createAndSaveSchedulableMovie();
        long screeningId = createScreening(movie.getId(), theater.getId());

        pipeline.send(new ChangeStateScreeningCommand(
                screeningId,
                ScreeningStateChange.OPEN_SALES,
                null
        ));

        ChangeActiveTheaterCommand command = new ChangeActiveTheaterCommand(
                theater.getId(),
                TheaterActiveChange.DEACTIVATE
        );

        assertThatThrownBy(() -> pipeline.send(command))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("비활성화할 수 없습니다.");

        Theater unchanged = theaterRepository.getById(theater.getId());
        assertThat(unchanged.isActive()).isTrue();
    }

    @Test
    @Transactional
    @DisplayName("SALES_CLOSED 상영이 연결된 극장은 비활성화할 수 없다")
    void changeActive_withSalesClosedScreening_throwsException() {
        Theater theater = createAndSaveTheater("4관", true);
        Movie movie = createAndSaveSchedulableMovie();
        long screeningId = createScreening(movie.getId(), theater.getId());

        pipeline.send(new ChangeStateScreeningCommand(
                screeningId,
                ScreeningStateChange.OPEN_SALES,
                null
        ));
        pipeline.send(new ChangeStateScreeningCommand(
                screeningId,
                ScreeningStateChange.CLOSE_SALES,
                null
        ));

        ChangeActiveTheaterCommand command = new ChangeActiveTheaterCommand(
                theater.getId(),
                TheaterActiveChange.DEACTIVATE
        );

        assertThatThrownBy(() -> pipeline.send(command))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("비활성화할 수 없습니다.");

        Theater unchanged = theaterRepository.getById(theater.getId());
        assertThat(unchanged.isActive()).isTrue();
    }
}
