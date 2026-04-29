package com.movie.shop.api.screening.infrastructure.policy;

import com.movie.shop.api.auditorium.domain.port.AuditoriumJpaPort;
import com.movie.shop.api.screening.domain.condition.AuditoriumScreeningCondition;
import com.movie.shop.api.screening.domain.port.AuditoriumScreeningConditionPort;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuditoriumScreeningConditionJpaAdapter implements AuditoriumScreeningConditionPort {

    private final AuditoriumJpaPort auditoriumJpaPort;

    @Override
    public Optional<AuditoriumScreeningCondition> findCondition(long auditoriumId) {
        return auditoriumJpaPort.findById(auditoriumId)
                .map(auditorium -> new AuditoriumScreeningCondition(
                        auditorium.getTheaterId(),
                        auditorium.canHostScreening()
                ));
    }
}
