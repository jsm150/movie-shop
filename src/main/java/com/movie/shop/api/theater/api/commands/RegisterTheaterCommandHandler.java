package com.movie.shop.api.theater.api.commands;

import an.awesome.pipelinr.Command;
import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.aggregate.TheaterRepository;
import com.movie.shop.api.theater.domain.port.TheaterNameUniquenessConditionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RegisterTheaterCommandHandler implements Command.Handler<RegisterTheaterCommand, Long> {

    private final TheaterRepository theaterRepository;
    private final TheaterNameUniquenessConditionPort theaterNameUniquenessConditionPort;

    @Override
    @Transactional
    public Long handle(RegisterTheaterCommand command) {
        var nameCondition = theaterNameUniquenessConditionPort.findCondition(command.name());
        var theater = Theater.register(
                command.name(),
                nameCondition
        );

        theaterRepository.save(theater);

        return theater.getId();
    }
}
