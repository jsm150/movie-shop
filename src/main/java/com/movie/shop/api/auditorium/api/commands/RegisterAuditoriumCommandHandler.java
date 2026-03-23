package com.movie.shop.api.auditorium.api.commands;

import an.awesome.pipelinr.Command;
import com.movie.shop.api.auditorium.domain.aggregate.Auditorium;
import com.movie.shop.api.auditorium.domain.aggregate.AuditoriumRepository;
import com.movie.shop.api.auditorium.domain.policy.AuditoriumNameDuplicatePolicy;
import com.movie.shop.api.auditorium.domain.policy.AuditoriumTheaterExistencePolicy;
import com.movie.shop.api.auditorium.domain.port.LoadAuditoriumTheaterExistenceStatusPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RegisterAuditoriumCommandHandler implements Command.Handler<RegisterAuditoriumCommand, Long> {

    private final AuditoriumRepository auditoriumRepository;
    private final AuditoriumNameDuplicatePolicy auditoriumNameDuplicatePolicy;
    private final LoadAuditoriumTheaterExistenceStatusPort loadAuditoriumTheaterExistenceStatusPort;

    @Override
    @Transactional
    public Long handle(RegisterAuditoriumCommand command) {
        AuditoriumTheaterExistencePolicy auditoriumTheaterExistencePolicy = new AuditoriumTheaterExistencePolicy(
                loadAuditoriumTheaterExistenceStatusPort.loadAuditoriumTheaterExistenceStatus(command.theaterId())
        );

        Auditorium auditorium = Auditorium.register(
                auditoriumNameDuplicatePolicy,
                auditoriumTheaterExistencePolicy,
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
