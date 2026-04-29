package com.movie.shop.api.auditorium.api.commands;

import an.awesome.pipelinr.Command;
import com.movie.shop.api.auditorium.domain.aggregate.Auditorium;
import com.movie.shop.api.auditorium.domain.aggregate.AuditoriumRepository;
import com.movie.shop.api.auditorium.domain.port.AuditoriumNameUniquenessConditionPort;
import com.movie.shop.api.auditorium.domain.port.AuditoriumRegistrationTheaterPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RegisterAuditoriumCommandHandler implements Command.Handler<RegisterAuditoriumCommand, Long> {

    private final AuditoriumRepository auditoriumRepository;
    private final AuditoriumNameUniquenessConditionPort auditoriumNameUniquenessConditionPort;
    private final AuditoriumRegistrationTheaterPort auditoriumRegistrationTheaterPort;

    @Override
    @Transactional
    public Long handle(RegisterAuditoriumCommand command) {
        var registrationTheater = auditoriumRegistrationTheaterPort.findRegistrationTheater(command.theaterId());
        var nameCondition = auditoriumNameUniquenessConditionPort.findCondition(command.theaterId(), command.name());

        Auditorium auditorium = Auditorium.register(
                nameCondition,
                registrationTheater,
                command.theaterId(),
                command.name(),
                command.floor(),
                command.auditoriumType(),
                command.seats(),
                command.rowCount(),
                command.columnCount()
        );

        auditoriumRepository.save(auditorium);
        return auditorium.getId();
    }
}
