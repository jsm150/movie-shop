package com.movie.shop.api.screening.api.commands;

import an.awesome.pipelinr.Pipeline;
import com.movie.shop.api.configuration.AbstractContainerBase;
import com.movie.shop.api.movie.domain.aggregate.Actor;
import com.movie.shop.api.movie.domain.aggregate.AudienceRating;
import com.movie.shop.api.movie.domain.aggregate.Movie;
import com.movie.shop.api.movie.domain.aggregate.MovieRepository;
import com.movie.shop.api.movie.domain.aggregate.MovieStatus;
import com.movie.shop.api.movie.domain.port.MovieJpaPort;
import com.movie.shop.api.movie.domain.policy.MovieTitleDuplication;
import com.movie.shop.api.movie.domain.policy.MovieTitleDuplicateValidator;
import com.movie.shop.api.screening.domain.aggregate.Screening;
import com.movie.shop.api.screening.domain.aggregate.ScreeningRepository;
import com.movie.shop.api.screening.domain.port.LoadMovieSchedulingAvailabilityPort;
import com.movie.shop.api.screening.domain.port.LoadTheaterScreeningAvailabilityPort;
import com.movie.shop.api.screening.domain.policy.MovieSchedulingAvailability;
import com.movie.shop.api.screening.domain.port.ScreeningJpaPort;
import com.movie.shop.api.screening.domain.policy.ScreeningScheduleValidationPolicy;
import com.movie.shop.api.screening.domain.policy.ScreeningTimeRuntimeValidationPolicy;
import com.movie.shop.api.theater.api.commands.ChangeActiveTheaterCommand;
import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.aggregate.TheaterActiveChange;
import com.movie.shop.api.theater.domain.aggregate.TheaterRepository;
import com.movie.shop.api.theater.domain.aggregate.TheaterType;
import com.movie.shop.api.theater.domain.port.TheaterJpaPort;
import com.movie.shop.api.theater.domain.policy.TheaterNameDuplicateValidator;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
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
    protected TheaterNameDuplicateValidator theaterNameDuplicateValidator;

    @Autowired
    protected LoadMovieSchedulingAvailabilityPort loadMovieSchedulingAvailabilityPort;

    @Autowired
    protected LoadTheaterScreeningAvailabilityPort loadTheaterScreeningAvailabilityPort;

    protected MovieTitleDuplicateValidator nonDuplicateTitleValidator() {
        return new MovieTitleDuplicateValidator(new MovieTitleDuplication(false));
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
            case COMING_SOON -> movie.moveToComingSoon();
            case NOW_SHOWING -> {
                movie.moveToComingSoon();
                movie.startShowing();
            }
            case ENDED -> {
                movie.moveToComingSoon();
                movie.startShowing();
                movie.endShowing();
            }
        }

        movie = movieRepository.save(movie);
        flushAndClear();
        return movie;
    }

    protected Theater createTheater(boolean active) {
        long seq = SEQUENCE.getAndIncrement();

        Theater theater = Theater.Register(
                theaterNameDuplicateValidator,
                "통합테스트관-" + seq,
                1,
                TheaterType.Standard,
                List.of("A1", "A2", "B1", "B2"),
                2,
                2
        );

        theater = theaterRepository.save(theater);
        flushAndClear();

        if (!active) {
            pipeline.send(new ChangeActiveTheaterCommand(theater.getId(), TheaterActiveChange.DEACTIVATE));
            flushAndClear();
            theater = theaterRepository.getById(theater.getId());
        }

        return theater;
    }

    protected Screening createScreening(long movieId,
                                        long theaterId,
                                        OffsetDateTime start,
                                        OffsetDateTime end,
                                        OffsetDateTime salesStart,
                                        OffsetDateTime salesEnd) {
        Optional<MovieSchedulingAvailability> movieSchedulingAvailability =
                loadMovieSchedulingAvailabilityPort.loadMovieSchedulingAvailability(movieId);

        ScreeningScheduleValidationPolicy screeningScheduleValidationPolicy = new ScreeningScheduleValidationPolicy(
                movieSchedulingAvailability,
                loadTheaterScreeningAvailabilityPort.loadTheaterScreeningAvailability(theaterId),
                screeningJpaPort.findConflictCandidatesByTheaterId(theaterId, start, end)
        );
        ScreeningTimeRuntimeValidationPolicy screeningTimeRuntimeValidationPolicy =
                new ScreeningTimeRuntimeValidationPolicy(movieSchedulingAvailability);

        Screening screening = Screening.register(
                screeningScheduleValidationPolicy,
                screeningTimeRuntimeValidationPolicy,
                movieId,
                theaterId,
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
}
