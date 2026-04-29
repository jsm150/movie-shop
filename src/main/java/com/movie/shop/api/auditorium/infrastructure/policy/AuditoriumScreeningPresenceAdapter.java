package com.movie.shop.api.auditorium.infrastructure.policy;

import com.movie.shop.api.auditorium.domain.condition.AuditoriumScreeningPresence;
import com.movie.shop.api.auditorium.domain.port.AuditoriumScreeningPresencePort;
import com.movie.shop.api.screening.domain.port.ScreeningJpaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditoriumScreeningPresenceAdapter implements AuditoriumScreeningPresencePort {

    private final ScreeningJpaPort screeningJpaPort;

    @Override
    public AuditoriumScreeningPresence findPresence(long auditoriumId) {
        boolean hasBlockingScreening = screeningJpaPort.findAllByAuditoriumId(auditoriumId).stream()
                .anyMatch(screening -> screening.blocksTheaterDeactivationOrDeletion());
        return new AuditoriumScreeningPresence(hasBlockingScreening);
    }
}
