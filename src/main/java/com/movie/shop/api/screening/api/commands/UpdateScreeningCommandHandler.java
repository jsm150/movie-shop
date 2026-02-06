package com.movie.shop.api.screening.api.commands;

import an.awesome.pipelinr.Command;
import com.movie.shop.api.screening.domain.aggregate.Screening;
import com.movie.shop.api.screening.domain.aggregate.ScreeningRepository;
import com.movie.shop.api.screening.domain.policy.ScreeningScheduleValidationPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdateScreeningCommandHandler implements Command.Handler<UpdateScreeningCommand, Long> {

    private final ScreeningRepository screeningRepository;
    private final ScreeningScheduleValidationPolicy screeningScheduleValidationPolicy;

    @Override
    @Transactional
    public Long handle(UpdateScreeningCommand command) {
        Screening screening = screeningRepository.getById(command.screeningId());

        screening.reschedule(
                screeningScheduleValidationPolicy,
                command.screeningStartTime(),
                command.screeningEndTime(),
                command.salesStartAt(),
                command.salesEndAt()
        );

        return screening.getId();
    }
}
