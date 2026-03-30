package com.movie.shop.api.theater.api.commands;

import an.awesome.pipelinr.Command;
import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.aggregate.TheaterRepository;
import com.movie.shop.api.theater.domain.port.TheaterJpaPort;
import com.movie.shop.api.theater.domain.policy.TheaterNamePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdateTheaterCommandHandler implements Command.Handler<UpdateTheaterCommand, Long> {

    private final TheaterRepository theaterRepository;
    private final TheaterJpaPort theaterJpaPort;

    @Override
    @Transactional
    public Long handle(UpdateTheaterCommand command) {
        Theater theater = theaterRepository.getById(command.theaterId());
        TheaterNamePolicy theaterNameDuplicateValidator = new TheaterNamePolicy(theaterJpaPort);

        theater.updateName(theaterNameDuplicateValidator, command.name());

        return command.theaterId();
    }
}
