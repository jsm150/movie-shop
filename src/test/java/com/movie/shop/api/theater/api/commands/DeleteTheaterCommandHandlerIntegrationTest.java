package com.movie.shop.api.theater.api.commands;

import an.awesome.pipelinr.Pipeline;
import com.movie.shop.api.configuration.AbstractContainerBase;
import com.movie.shop.api.movie.domain.aggregate.Actor;
import com.movie.shop.api.movie.domain.aggregate.AudienceRating;
import com.movie.shop.api.movie.domain.aggregate.Movie;
import com.movie.shop.api.movie.domain.aggregate.MovieRepository;
import com.movie.shop.api.movie.domain.policy.MovieTitleDuplicateValidator;
import com.movie.shop.api.screening.api.commands.RegisterScreeningCommand;
import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.aggregate.TheaterRepository;
import com.movie.shop.api.theater.domain.aggregate.TheaterType;
import com.movie.shop.api.theater.domain.port.TheaterJpaPort;
import com.movie.shop.api.theater.domain.policy.TheaterNameDuplicateValidator;
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
class DeleteTheaterCommandHandlerIntegrationTest extends AbstractContainerBase {

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
    private TheaterJpaPort theaterJpaPort;

    @Autowired
    private EntityManager entityManager;

    private Theater createAndSaveTheater(String name) {
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
        return theater;
    }

    private Movie createAndSaveSchedulableMovie() {
        long seq = SEQUENCE.getAndIncrement();

        Movie movie = Movie.Register(
                movieTitleDuplicateValidator,
                "극장삭제테스트영화-" + seq,
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

    @Test
    @DisplayName("존재하는 상영관을 삭제하면 DB에서 상영관이 제거된다")
    @Transactional
    void deleteTheater_removesTheaterFromDatabase() {
        // Given
        Theater theater = createAndSaveTheater("1관");

        long theaterId = theater.getId();

        assertThat(theaterJpaPort.findById(theaterId)).isPresent();

        // When
        DeleteTheaterCommand command = new DeleteTheaterCommand(theaterId);
        pipeline.send(command);

        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(theaterJpaPort.findById(theaterId)).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 상영관 ID로 삭제를 요청하면 예외가 발생한다")
    @Transactional
    void deleteTheater_withNonExistentId_throwsException() {
        // Given
        long nonExistentTheaterId = 999999L;

        // When & Then
        DeleteTheaterCommand command = new DeleteTheaterCommand(nonExistentTheaterId);
        assertThatThrownBy(() -> pipeline.send(command))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("상영관 데이터가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("연결된 상영이 있는 상영관을 삭제하면 예외가 발생한다")
    @Transactional
    void deleteTheater_withScheduledScreening_throwsException() {
        Theater theater = createAndSaveTheater("2관");
        Movie movie = createAndSaveSchedulableMovie();

        RegisterScreeningCommand registerScreeningCommand = new RegisterScreeningCommand(
                movie.getId(),
                theater.getId(),
                OffsetDateTime.parse("2026-03-01T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T12:00:00Z"),
                OffsetDateTime.parse("2026-02-20T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T10:00:00Z")
        );
        pipeline.send(registerScreeningCommand);

        DeleteTheaterCommand command = new DeleteTheaterCommand(theater.getId());

        assertThatThrownBy(() -> pipeline.send(command))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("삭제할 수 없습니다.");
        assertThat(theaterJpaPort.findById(theater.getId())).isPresent();
    }
}
