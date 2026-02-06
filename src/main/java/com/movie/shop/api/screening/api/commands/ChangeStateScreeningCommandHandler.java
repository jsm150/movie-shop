package com.movie.shop.api.screening.api.commands;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Voidy;
import com.movie.shop.api.screening.domain.aggregate.Screening;
import com.movie.shop.api.screening.domain.aggregate.ScreeningRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class ChangeStateScreeningCommandHandler implements Command.Handler<ChangeStateScreeningCommand, Voidy> {

    private final ScreeningRepository screeningRepository;

    @Override
    @Transactional
    public Voidy handle(ChangeStateScreeningCommand command) {
        Screening screening = screeningRepository.getById(command.screeningId());
        OffsetDateTime now = OffsetDateTime.now();

        switch (command.status()) {
            case OPEN_SALES -> screening.openSales();
            case CLOSE_SALES -> screening.closeSales();
            case CANCEL -> screening.cancel(command.cancelReason(), now);
            case FINISH -> screening.finish(now);
        }

        return null;
    }
}
