package com.movie.shop.api.theater.infrastructure.policy;

import com.movie.shop.api.screening.domain.aggregate.Screening;
import com.movie.shop.api.screening.domain.aggregate.port.ScreeningJpaPort;
import com.movie.shop.api.theater.domain.policy.port.CheckTheaterScreeningLinkPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckTheaterScreeningLinkJpaAdapter implements CheckTheaterScreeningLinkPort {

    private final ScreeningJpaPort screeningJpaPort;

    @Override
    public boolean existsBlockingScreeningByTheaterId(long theaterId) {
        return screeningJpaPort.findAllByTheaterId(theaterId)
                .stream()
                .anyMatch(Screening::blocksTheaterDeactivationOrDeletion);
    }
}
