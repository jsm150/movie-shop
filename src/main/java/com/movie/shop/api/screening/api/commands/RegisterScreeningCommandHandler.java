package com.movie.shop.api.screening.api.commands;

import an.awesome.pipelinr.Command;
import com.movie.shop.api.screening.domain.aggregate.Screening;
import com.movie.shop.api.screening.domain.aggregate.ScreeningRepository;
import com.movie.shop.api.screening.domain.port.LoadMovieSchedulingAvailabilityPort;
import com.movie.shop.api.screening.domain.port.LoadTheaterScreeningAvailabilityPort;
import com.movie.shop.api.screening.domain.policy.MovieSchedulingAvailability;
import com.movie.shop.api.screening.domain.port.ScreeningJpaPort;
import com.movie.shop.api.screening.domain.policy.ScreeningScheduleValidationPolicy;
import com.movie.shop.api.screening.domain.policy.ScreeningTimeRuntimeValidationPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RegisterScreeningCommandHandler implements Command.Handler<RegisterScreeningCommand, Long> {

    private final ScreeningRepository screeningRepository;
    private final LoadMovieSchedulingAvailabilityPort loadMovieSchedulingAvailabilityPort;
    private final LoadTheaterScreeningAvailabilityPort loadTheaterScreeningAvailabilityPort;
    private final ScreeningJpaPort screeningJpaPort;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Long handle(RegisterScreeningCommand command) {
        Optional<MovieSchedulingAvailability> movieSchedulingAvailability =
                loadMovieSchedulingAvailabilityPort.loadMovieSchedulingAvailability(command.movieId());

        ScreeningScheduleValidationPolicy screeningScheduleValidationPolicy = new ScreeningScheduleValidationPolicy(
                movieSchedulingAvailability,
                loadTheaterScreeningAvailabilityPort.loadTheaterScreeningAvailability(command.theaterId()),
                screeningJpaPort.findConflictCandidatesByTheaterId(
                        command.theaterId(),
                        command.screeningStartTime(),
                        command.screeningEndTime()
                )
        );
        ScreeningTimeRuntimeValidationPolicy screeningTimeRuntimeValidationPolicy =
                new ScreeningTimeRuntimeValidationPolicy(movieSchedulingAvailability);

        Screening screening = Screening.register(
                screeningScheduleValidationPolicy,
                screeningTimeRuntimeValidationPolicy,
                command.movieId(),
                command.theaterId(),
                command.screeningStartTime(),
                command.screeningEndTime(),
                command.salesStartAt(),
                command.salesEndAt()
        );

        screeningRepository.save(screening);

        return screening.getId();
    }
}
