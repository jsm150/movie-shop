package com.movie.shop.api.screening.api.commands;

import an.awesome.pipelinr.Command;
import com.movie.shop.api.screening.domain.aggregate.Screening;
import com.movie.shop.api.screening.domain.aggregate.ScreeningRepository;
import com.movie.shop.api.screening.domain.port.LoadMovieSchedulingAvailabilityPort;
import com.movie.shop.api.screening.domain.port.LoadAuditoriumScreeningAvailabilityPort;
import com.movie.shop.api.screening.domain.port.LoadScreeningConflictCandidatesPort;
import com.movie.shop.api.screening.domain.port.MemoizedMovieSchedulingAvailabilityPort;
import com.movie.shop.api.screening.domain.policy.ScreeningConflictValidationPolicy;
import com.movie.shop.api.screening.domain.policy.ScreeningScheduleValidationPolicy;
import com.movie.shop.api.screening.domain.policy.ScreeningTimeRuntimeValidationPolicy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdateScreeningCommandHandler implements Command.Handler<UpdateScreeningCommand, Long> {

    private final ScreeningRepository screeningRepository;
    private final LoadMovieSchedulingAvailabilityPort loadMovieSchedulingAvailabilityPort;
    private final LoadAuditoriumScreeningAvailabilityPort loadAuditoriumScreeningAvailabilityPort;
    private final LoadScreeningConflictCandidatesPort loadScreeningConflictCandidatesPort;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Long handle(UpdateScreeningCommand command) {
        Screening screening = screeningRepository.getById(command.screeningId());

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
