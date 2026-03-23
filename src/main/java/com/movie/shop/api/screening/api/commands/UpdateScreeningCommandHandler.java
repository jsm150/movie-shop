package com.movie.shop.api.screening.api.commands;

import an.awesome.pipelinr.Command;
import com.movie.shop.api.screening.domain.aggregate.Screening;
import com.movie.shop.api.screening.domain.aggregate.ScreeningRepository;
import com.movie.shop.api.screening.domain.port.LoadMovieSchedulingAvailabilityPort;
import com.movie.shop.api.screening.domain.port.LoadAuditoriumScreeningAvailabilityPort;
import com.movie.shop.api.screening.domain.policy.ScreeningConflictValidationPolicy;
import com.movie.shop.api.screening.domain.port.ScreeningJpaPort;
import com.movie.shop.api.screening.domain.policy.ScreeningScheduleValidationPolicy;
import com.movie.shop.api.screening.domain.policy.ScreeningTimeRuntimeValidationPolicy;
import com.movie.shop.api.screening.domain.policy.status.MovieSchedulingAvailability;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UpdateScreeningCommandHandler implements Command.Handler<UpdateScreeningCommand, Long> {

    private final ScreeningRepository screeningRepository;
    private final LoadMovieSchedulingAvailabilityPort loadMovieSchedulingAvailabilityPort;
    private final LoadAuditoriumScreeningAvailabilityPort loadAuditoriumScreeningAvailabilityPort;
    private final ScreeningJpaPort screeningJpaPort;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Long handle(UpdateScreeningCommand command) {
        Screening screening = screeningRepository.getById(command.screeningId());

        Optional<MovieSchedulingAvailability> movieSchedulingAvailability =
                loadMovieSchedulingAvailabilityPort.loadMovieSchedulingAvailability(screening.getMovieId());

        ScreeningScheduleValidationPolicy screeningScheduleValidationPolicy = new ScreeningScheduleValidationPolicy(
                movieSchedulingAvailability,
                loadAuditoriumScreeningAvailabilityPort.loadTheaterScreeningAvailability(screening.getTheaterId())
        );
        ScreeningConflictValidationPolicy screeningConflictValidationPolicy = new ScreeningConflictValidationPolicy(
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
                screeningConflictValidationPolicy,
                screeningTimeRuntimeValidationPolicy,
                command.screeningStartTime(),
                command.screeningEndTime(),
                command.salesStartAt(),
                command.salesEndAt()
        );

        return screening.getId();
    }
}
