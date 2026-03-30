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
public class RegisterScreeningCommandHandler implements Command.Handler<RegisterScreeningCommand, Long> {

    private final ScreeningRepository screeningRepository;
    private final LoadMovieSchedulingAvailabilityPort loadMovieSchedulingAvailabilityPort;
    private final LoadAuditoriumScreeningAvailabilityPort loadAuditoriumScreeningAvailabilityPort;
    private final LoadScreeningConflictCandidatesPort loadScreeningConflictCandidatesPort;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Long handle(RegisterScreeningCommand command) {
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
                command.movieId(),
                command.auditoriumId(),
                command.screeningStartTime(),
                command.screeningEndTime(),
                command.salesStartAt(),
                command.salesEndAt()
        );

        screeningRepository.save(screening);

        return screening.getId();
    }
}
