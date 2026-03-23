package com.movie.shop.api.auditorium.api.commands;

import an.awesome.pipelinr.Command;
import com.movie.shop.api.auditorium.domain.aggregate.Auditorium;
import com.movie.shop.api.auditorium.domain.aggregate.AuditoriumRepository;
import com.movie.shop.api.auditorium.domain.policy.AuditoriumNameDuplicatePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdateAuditoriumCommandHandler implements Command.Handler<UpdateAuditoriumCommand, Long> {

    private final AuditoriumRepository auditoriumRepository;
    private final AuditoriumNameDuplicatePolicy auditoriumNameDuplicatePolicy;

    @Override
    @Transactional
    public Long handle(UpdateAuditoriumCommand command) {
        Auditorium auditorium = auditoriumRepository.getById(command.auditoriumId());

        auditorium.update(
                auditoriumNameDuplicatePolicy,
                command.name(),
                command.floor(),
                command.auditoriumType(),
                command.seats(),
                command.rowCount(),
                command.columnCount()
        );

        return auditorium.getId();
    }
}
