package com.movie.shop.api.theater.api.commands;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Voidy;
import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.aggregate.TheaterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ChangeActiveTheaterCommandHandler implements Command.Handler<ChangeActiveTheaterCommand, Voidy> {

    private final TheaterRepository theaterRepository;

    @Override
    @Transactional
    public Voidy handle(ChangeActiveTheaterCommand command) {
        Theater theater = theaterRepository.getById(command.theaterId());

        switch (command.status()) {
            case ACTIVATE -> theater.activate();
            case DEACTIVATE -> theater.deactivate();
        }

        return null;
    }
}
