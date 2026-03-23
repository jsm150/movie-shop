package com.movie.shop.api.auditorium.api.commands;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Voidy;
import com.movie.shop.api.auditorium.domain.aggregate.Auditorium;
import com.movie.shop.api.auditorium.domain.aggregate.AuditoriumRepository;
import com.movie.shop.api.auditorium.domain.policy.AuditoriumStatusAndDeletionPolicy;
import com.movie.shop.api.auditorium.domain.port.CheckAuditoriumScreeningLinkPort;
import com.movie.shop.api.auditorium.domain.port.LoadAuditoriumTheaterActivationStatusPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ChangeStatusAuditoriumCommandHandler implements Command.Handler<ChangeStatusAuditoriumCommand, Voidy> {

    private final AuditoriumRepository auditoriumRepository;
    private final CheckAuditoriumScreeningLinkPort checkAuditoriumScreeningLinkPort;
    private final LoadAuditoriumTheaterActivationStatusPort loadAuditoriumTheaterActivationStatusPort;

    @Override
    @Transactional
    public Voidy handle(ChangeStatusAuditoriumCommand command) {
        Auditorium auditorium = auditoriumRepository.getById(command.auditoriumId());

        AuditoriumStatusAndDeletionPolicy auditoriumStatusAndDeletionPolicy = new AuditoriumStatusAndDeletionPolicy(
                checkAuditoriumScreeningLinkPort.loadAuditoriumScreeningLinkStatus(command.auditoriumId()),
                loadAuditoriumTheaterActivationStatusPort.loadAuditoriumTheaterActivationStatus(auditorium.getTheaterId())
        );

        auditorium.changeStatus(command.status(), auditoriumStatusAndDeletionPolicy);
        return null;
    }
}
