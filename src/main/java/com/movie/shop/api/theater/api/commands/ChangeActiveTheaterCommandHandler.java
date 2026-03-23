package com.movie.shop.api.theater.api.commands;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Voidy;
import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.aggregate.TheaterRepository;
import com.movie.shop.api.theater.domain.port.CheckTheaterAuditoriumLinkPort;
import com.movie.shop.api.theater.domain.policy.TheaterAuditoriumLinkProtectionPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ChangeActiveTheaterCommandHandler implements Command.Handler<ChangeActiveTheaterCommand, Voidy> {

    private final TheaterRepository theaterRepository;
    private final CheckTheaterAuditoriumLinkPort checkTheaterAuditoriumLinkPort;

    @Override
    @Transactional
    public Voidy handle(ChangeActiveTheaterCommand command) {
        Theater theater = theaterRepository.getById(command.theaterId());
        TheaterAuditoriumLinkProtectionPolicy theaterAuditoriumLinkProtectionPolicy =
                new TheaterAuditoriumLinkProtectionPolicy(
                        checkTheaterAuditoriumLinkPort.loadTheaterAuditoriumLinkStatus(command.theaterId())
                );
        theater.changeActive(command.status(), theaterAuditoriumLinkProtectionPolicy);

        return null;
    }
}
