package com.movie.shop.api.screening.api.commands;

import an.awesome.pipelinr.Command;
import com.movie.shop.api.screening.domain.aggregate.Screening;
import com.movie.shop.api.screening.domain.aggregate.ScreeningRepository;
import com.movie.shop.api.screening.domain.port.AuditoriumScreeningConditionPort;
import com.movie.shop.api.screening.domain.port.MovieSchedulingConditionPort;
import com.movie.shop.api.screening.domain.port.ScreeningOverlapCandidatesPort;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RegisterScreeningCommandHandler implements Command.Handler<RegisterScreeningCommand, Long> {

    private final ScreeningRepository screeningRepository;
    private final MovieSchedulingConditionPort movieSchedulingConditionPort;
    private final AuditoriumScreeningConditionPort auditoriumScreeningConditionPort;
    private final ScreeningOverlapCandidatesPort screeningOverlapCandidatesPort;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Long handle(RegisterScreeningCommand command) {
        var movieSchedulingCondition = movieSchedulingConditionPort.findCondition(command.movieId());
        var auditoriumScreeningCondition = auditoriumScreeningConditionPort.findCondition(command.auditoriumId());
        var overlapCandidates = screeningOverlapCandidatesPort.findOverlapCandidates(
                command.auditoriumId(),
                command.screeningStartTime(),
                command.screeningEndTime()
        );

        Screening screening = Screening.register(
                command.movieId(),
                command.auditoriumId(),
                movieSchedulingCondition,
                auditoriumScreeningCondition,
                overlapCandidates,
                command.screeningStartTime(),
                command.screeningEndTime(),
                command.salesStartAt(),
                command.salesEndAt()
        );

        screeningRepository.save(screening);

        return screening.getId();
    }
}
