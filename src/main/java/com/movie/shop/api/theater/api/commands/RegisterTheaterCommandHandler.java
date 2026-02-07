package com.movie.shop.api.theater.api.commands;

import an.awesome.pipelinr.Command;
import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.aggregate.TheaterRepository;
import com.movie.shop.api.theater.domain.policy.TheaterNameDuplicateValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RegisterTheaterCommandHandler implements Command.Handler<RegisterTheaterCommand, Long> {

    private final TheaterRepository theaterRepository;
    private final TheaterNameDuplicateValidator theaterNameDuplicateValidator;

    @Override
    @Transactional
    public Long handle(RegisterTheaterCommand command) {
        var theater = Theater.Register(
                theaterNameDuplicateValidator,
                command.name(),
                command.floor(),
                command.type(),
                command.seats(),
                command.rowCount(),
                command.columnCount()
        );

        theaterRepository.save(theater);

        return theater.getId();
    }
}
