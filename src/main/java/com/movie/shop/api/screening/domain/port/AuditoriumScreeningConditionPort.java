package com.movie.shop.api.screening.domain.port;

import java.util.Optional;

import com.movie.shop.api.screening.domain.condition.AuditoriumScreeningCondition;

public interface AuditoriumScreeningConditionPort {
    Optional<AuditoriumScreeningCondition> findCondition(long auditoriumId);
}
