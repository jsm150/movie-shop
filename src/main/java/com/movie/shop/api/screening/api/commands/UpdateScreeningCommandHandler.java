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
public class UpdateScreeningCommandHandler implements Command.Handler<UpdateScreeningCommand, Long> {

    private final ScreeningRepository screeningRepository;
    private final MovieSchedulingConditionPort movieSchedulingConditionPort;
    private final AuditoriumScreeningConditionPort auditoriumScreeningConditionPort;
    private final ScreeningOverlapCandidatesPort screeningOverlapCandidatesPort;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Long handle(UpdateScreeningCommand command) {
        Screening screening = screeningRepository.getById(command.screeningId());
        var movieSchedulingCondition = movieSchedulingConditionPort.findCondition(screening.getMovieId());
        var auditoriumScreeningCondition = auditoriumScreeningConditionPort.findCondition(screening.getAuditoriumId());
        var overlapCandidates = screeningOverlapCandidatesPort.findOverlapCandidates(
                screening.getAuditoriumId(),
                command.screeningStartTime(),
                command.screeningEndTime()
        );

        screening.reschedule(
                movieSchedulingCondition,
                auditoriumScreeningCondition,
                overlapCandidates,
                command.screeningStartTime(),
                command.screeningEndTime(),
                command.salesStartAt(),
                command.salesEndAt()
        );

        return screening.getId();
    }
}
