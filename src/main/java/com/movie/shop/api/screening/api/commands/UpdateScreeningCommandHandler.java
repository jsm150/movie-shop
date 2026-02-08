package com.movie.shop.api.screening.api.commands;

import an.awesome.pipelinr.Command;
import com.movie.shop.api.screening.domain.aggregate.Screening;
import com.movie.shop.api.screening.domain.aggregate.ScreeningRepository;
import com.movie.shop.api.screening.domain.port.LoadMovieSchedulingAvailabilityPort;
import com.movie.shop.api.screening.domain.port.LoadTheaterScreeningAvailabilityPort;
import com.movie.shop.api.screening.domain.port.MovieSchedulingAvailability;
import com.movie.shop.api.screening.domain.port.ScreeningJpaPort;
import com.movie.shop.api.screening.domain.policy.ScreeningScheduleValidationPolicy;
import com.movie.shop.api.screening.domain.policy.ScreeningTimeRuntimeValidationPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UpdateScreeningCommandHandler implements Command.Handler<UpdateScreeningCommand, Long> {

    private final ScreeningRepository screeningRepository;
    private final LoadMovieSchedulingAvailabilityPort loadMovieSchedulingAvailabilityPort;
    private final LoadTheaterScreeningAvailabilityPort loadTheaterScreeningAvailabilityPort;
    private final ScreeningJpaPort screeningJpaPort;

    @Override
    @Transactional
    public Long handle(UpdateScreeningCommand command) {
        Screening screening = screeningRepository.getById(command.screeningId());

        Optional<MovieSchedulingAvailability> movieSchedulingAvailability =
                loadMovieSchedulingAvailabilityPort.loadMovieSchedulingAvailability(screening.getMovieId());

        ScreeningScheduleValidationPolicy screeningScheduleValidationPolicy = new ScreeningScheduleValidationPolicy(
                movieSchedulingAvailability,
                loadTheaterScreeningAvailabilityPort.loadTheaterScreeningAvailability(screening.getTheaterId()),
                screeningJpaPort.findConflictCandidatesByTheaterIdAndIdNot(
                        screening.getTheaterId(),
                        command.screeningStartTime(),
                        command.screeningEndTime(),
                        screening.getId()
                )
        );
        ScreeningTimeRuntimeValidationPolicy screeningTimeRuntimeValidationPolicy =
                new ScreeningTimeRuntimeValidationPolicy(movieSchedulingAvailability);

        screening.reschedule(
                screeningScheduleValidationPolicy,
                screeningTimeRuntimeValidationPolicy,
                command.screeningStartTime(),
                command.screeningEndTime(),
                command.salesStartAt(),
                command.salesEndAt()
        );

        return screening.getId();
    }
}
