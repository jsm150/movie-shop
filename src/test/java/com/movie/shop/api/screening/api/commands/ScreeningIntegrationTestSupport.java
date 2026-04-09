package com.movie.shop.api.screening.api.commands;

import an.awesome.pipelinr.Pipeline;
import com.movie.shop.api.configuration.AbstractContainerBase;
import com.movie.shop.api.movie.domain.aggregate.Actor;
import com.movie.shop.api.movie.domain.aggregate.AudienceRating;
import com.movie.shop.api.movie.domain.aggregate.Movie;
import com.movie.shop.api.movie.domain.aggregate.MovieRepository;
import com.movie.shop.api.movie.domain.aggregate.MovieStateChange;
import com.movie.shop.api.movie.domain.aggregate.MovieStatus;
import com.movie.shop.api.movie.domain.port.MovieJpaPort;
import com.movie.shop.api.movie.domain.policy.MovieTitlePolicy;
import com.movie.shop.api.screening.domain.aggregate.Screening;
import com.movie.shop.api.screening.domain.aggregate.ScreeningRepository;
import com.movie.shop.api.screening.domain.port.LoadMovieSchedulingAvailabilityPort;
import com.movie.shop.api.screening.domain.port.LoadAuditoriumScreeningAvailabilityPort;
import com.movie.shop.api.screening.domain.port.LoadScreeningConflictCandidatesPort;
import com.movie.shop.api.screening.domain.port.MemoizedMovieSchedulingAvailabilityPort;
import com.movie.shop.api.screening.domain.policy.ScreeningConflictValidationPolicy;
import com.movie.shop.api.screening.domain.port.ScreeningJpaPort;
import com.movie.shop.api.screening.domain.policy.ScreeningScheduleValidationPolicy;
import com.movie.shop.api.screening.domain.policy.ScreeningTimeRuntimeValidationPolicy;
import com.movie.shop.api.theater.api.commands.ChangeActiveTheaterCommand;
import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.aggregate.TheaterActiveChange;
import com.movie.shop.api.theater.domain.aggregate.TheaterRepository;
import com.movie.shop.api.theater.domain.port.TheaterJpaPort;
import com.movie.shop.api.theater.domain.policy.TheaterNamePolicy;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

abstract class ScreeningIntegrationTestSupport extends AbstractContainerBase {

    private static final AtomicLong SEQUENCE = new AtomicLong(1L);

    @Autowired
    protected Pipeline pipeline;

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected MovieRepository movieRepository;

    @Autowired
    protected MovieJpaPort movieJpaPort;

    @Autowired
    protected TheaterRepository theaterRepository;

    @Autowired
    protected TheaterJpaPort theaterJpaPort;

    @Autowired
    protected ScreeningRepository screeningRepository;

    @Autowired
    protected ScreeningJpaPort screeningJpaPort;

    @Autowired
    protected LoadMovieSchedulingAvailabilityPort loadMovieSchedulingAvailabilityPort;

    @Autowired
    protected LoadAuditoriumScreeningAvailabilityPort loadAuditoriumScreeningAvailabilityPort;

    @Autowired
    protected LoadScreeningConflictCandidatesPort loadScreeningConflictCandidatesPort;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected MovieTitlePolicy nonDuplicateTitleValidator() {
        return new MovieTitlePolicy(movieJpaPort);
    }

    protected Movie createMovie(MovieStatus status) {
        long seq = SEQUENCE.getAndIncrement();

        Movie movie = Movie.Register(
                nonDuplicateTitleValidator(),
                "통합테스트영화-" + seq,
                "테스트 감독",
                List.of("드라마"),
                120,
                AudienceRating.PG12,
                "통합 테스트용 영화",
                OffsetDateTime.parse("2026-01-01T00:00:00Z"),
                List.of(
                        new Actor(
                                "테스트 배우-" + seq,
                                OffsetDateTime.parse("1990-01-01T00:00:00Z"),
                                "Korea",
                                "주인공"
                        )
                )
        );

        switch (status) {
            case PREPARING -> {
            }
            case COMING_SOON -> movie.changeState(MovieStateChange.COMING_SOON);
            case NOW_SHOWING -> {
                movie.changeState(MovieStateChange.COMING_SOON);
                movie.changeState(MovieStateChange.NOW_SHOWING);
            }
            case ENDED -> {
                movie.changeState(MovieStateChange.COMING_SOON);
                movie.changeState(MovieStateChange.NOW_SHOWING);
                movie.changeState(MovieStateChange.ENDED);
            }
        }

        movie = movieRepository.save(movie);
        flushAndClear();
        return movie;
    }

    protected Theater createTheater(boolean active) {
        long seq = SEQUENCE.getAndIncrement();
        String theaterName = "통합테스트관-" + seq;
        TheaterNamePolicy theaterNameDuplicateValidator = new TheaterNamePolicy(theaterJpaPort);

        Theater theater = Theater.register(theaterNameDuplicateValidator, theaterName);

        theater = theaterRepository.save(theater);
        flushAndClear();

        if (!active) {
            pipeline.send(new ChangeActiveTheaterCommand(theater.getId(), TheaterActiveChange.DEACTIVATE));
            flushAndClear();
            theater = theaterRepository.getById(theater.getId());
        }

        return theater;
    }

    protected long createAuditorium(boolean active) {
        long seq = SEQUENCE.getAndIncrement();

        Theater theater = createTheater(true);
        String auditoriumName = "통합테스트상영관-" + seq;

        jdbcTemplate.update(
                """
                INSERT INTO auditorium
                (theater_id, name, floor, auditorium_type, is_active, seats, row_count, column_count)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                theater.getId(),
                auditoriumName,
                1,
                "Standard",
                active,
                "[\"A1\",\"A2\"]",
                1,
                2
        );

        Long auditoriumId = jdbcTemplate.queryForObject(
                "SELECT auditorium_id FROM auditorium WHERE theater_id = ? AND name = ?",
                Long.class,
                theater.getId(),
                auditoriumName
        );

        if (auditoriumId == null) {
            throw new IllegalStateException("상영관 ID를 조회할 수 없습니다.");
        }

        flushAndClear();
        return auditoriumId;
    }

    protected Screening createScreening(long movieId,
                                        long auditoriumId,
                                        OffsetDateTime start,
                                        OffsetDateTime end,
                                        OffsetDateTime salesStart,
                                        OffsetDateTime salesEnd) {
        MemoizedMovieSchedulingAvailabilityPort memoizedMovieSchedulingAvailabilityPort =
                new MemoizedMovieSchedulingAvailabilityPort(loadMovieSchedulingAvailabilityPort);

        ScreeningScheduleValidationPolicy screeningScheduleValidationPolicy = new ScreeningScheduleValidationPolicy(
                memoizedMovieSchedulingAvailabilityPort,
                loadAuditoriumScreeningAvailabilityPort
        );
        ScreeningConflictValidationPolicy screeningConflictValidationPolicy =
                new ScreeningConflictValidationPolicy(loadScreeningConflictCandidatesPort);
        ScreeningTimeRuntimeValidationPolicy screeningTimeRuntimeValidationPolicy =
                new ScreeningTimeRuntimeValidationPolicy(memoizedMovieSchedulingAvailabilityPort);

        Screening screening = Screening.register(
                screeningScheduleValidationPolicy,
                screeningConflictValidationPolicy,
                screeningTimeRuntimeValidationPolicy,
                movieId,
                auditoriumId,
                loadTheaterIdByAuditoriumId(auditoriumId),
                start,
                end,
                salesStart,
                salesEnd
        );

        screening = screeningRepository.save(screening);
        flushAndClear();
        return screening;
    }

    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    protected long loadTheaterIdByAuditoriumId(long auditoriumId) {
        Long theaterId = jdbcTemplate.queryForObject(
                "SELECT theater_id FROM auditorium WHERE auditorium_id = ?",
                Long.class,
                auditoriumId
        );

        if (theaterId == null) {
            throw new IllegalStateException("영화관 ID를 조회할 수 없습니다.");
        }

        return theaterId;
    }
}
