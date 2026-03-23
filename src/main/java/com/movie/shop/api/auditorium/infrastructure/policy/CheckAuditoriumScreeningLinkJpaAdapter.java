package com.movie.shop.api.auditorium.infrastructure.policy;

import com.movie.shop.api.auditorium.domain.policy.status.AuditoriumScreeningLinkStatus;
import com.movie.shop.api.auditorium.domain.port.CheckAuditoriumScreeningLinkPort;
import com.movie.shop.api.screening.domain.port.ScreeningJpaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckAuditoriumScreeningLinkJpaAdapter implements CheckAuditoriumScreeningLinkPort {

    private final ScreeningJpaPort screeningJpaPort;

    @Override
    public AuditoriumScreeningLinkStatus loadAuditoriumScreeningLinkStatus(long auditoriumId) {
        boolean blockingScreeningExists = screeningJpaPort.findAllByAuditoriumId(auditoriumId).stream()
                .anyMatch(screening -> screening.blocksTheaterDeactivationOrDeletion());

        return new AuditoriumScreeningLinkStatus(blockingScreeningExists);
    }
}
