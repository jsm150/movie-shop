package com.movie.shop.api.screening.api.commands;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Voidy;
import com.movie.shop.api.screening.domain.aggregate.ScreeningRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DeleteScreeningCommandHandler implements Command.Handler<DeleteScreeningCommand, Voidy> {

    private final ScreeningRepository screeningRepository;

    @Override
    @Transactional
    public Voidy handle(DeleteScreeningCommand command) {
        screeningRepository.removeScheduledById(command.screeningId());
        return null;
    }
}