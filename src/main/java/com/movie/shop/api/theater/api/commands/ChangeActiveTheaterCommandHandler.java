package com.movie.shop.api.theater.api.commands;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Voidy;
import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.aggregate.TheaterRepository;
import com.movie.shop.api.theater.domain.port.TheaterAuditoriumPresencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ChangeActiveTheaterCommandHandler implements Command.Handler<ChangeActiveTheaterCommand, Voidy> {

    private final TheaterRepository theaterRepository;
    private final TheaterAuditoriumPresencePort theaterAuditoriumPresencePort;

    @Override
    @Transactional
    public Voidy handle(ChangeActiveTheaterCommand command) {
        Theater theater = theaterRepository.getById(command.theaterId());
        var auditoriumPresence = theaterAuditoriumPresencePort.findPresence(theater.getId());
        theater.changeActive(command.status(), auditoriumPresence);

        return null;
    }
}
