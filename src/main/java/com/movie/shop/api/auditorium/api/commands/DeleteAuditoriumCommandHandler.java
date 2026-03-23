package com.movie.shop.api.auditorium.api.commands;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Voidy;
import com.movie.shop.api.auditorium.domain.aggregate.Auditorium;
import com.movie.shop.api.auditorium.domain.aggregate.AuditoriumRepository;
import com.movie.shop.api.auditorium.domain.policy.AuditoriumDeletionPolicy;
import com.movie.shop.api.auditorium.domain.port.CheckAuditoriumScreeningLinkPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DeleteAuditoriumCommandHandler implements Command.Handler<DeleteAuditoriumCommand, Voidy> {

    private final AuditoriumRepository auditoriumRepository;
    private final CheckAuditoriumScreeningLinkPort checkAuditoriumScreeningLinkPort;

    @Override
    @Transactional
    public Voidy handle(DeleteAuditoriumCommand command) {
        Auditorium auditorium = auditoriumRepository.getById(command.auditoriumId());

        AuditoriumDeletionPolicy auditoriumDeletionPolicy = new AuditoriumDeletionPolicy(
                checkAuditoriumScreeningLinkPort.loadAuditoriumScreeningLinkStatus(command.auditoriumId())
        );

        auditoriumRepository.delete(auditorium, auditoriumDeletionPolicy);
        return null;
    }
}
