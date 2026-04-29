package com.movie.shop.api.auditorium.api.commands;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Voidy;
import com.movie.shop.api.auditorium.domain.aggregate.Auditorium;
import com.movie.shop.api.auditorium.domain.aggregate.AuditoriumRepository;
import com.movie.shop.api.auditorium.domain.port.AuditoriumScreeningPresencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DeleteAuditoriumCommandHandler implements Command.Handler<DeleteAuditoriumCommand, Voidy> {

    private final AuditoriumRepository auditoriumRepository;
    private final AuditoriumScreeningPresencePort auditoriumScreeningPresencePort;

    @Override
    @Transactional
    public Voidy handle(DeleteAuditoriumCommand command) {
        Auditorium auditorium = auditoriumRepository.getById(command.auditoriumId());
        var screeningPresence = auditoriumScreeningPresencePort.findPresence(auditorium.getId());

        auditoriumRepository.delete(auditorium, screeningPresence);
        return null;
    }
}
