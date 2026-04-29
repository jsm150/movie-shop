package com.movie.shop.api.auditorium.domain.port;

import com.movie.shop.api.auditorium.domain.condition.AuditoriumNameUniquenessCondition;

public interface AuditoriumNameUniquenessConditionPort {

    AuditoriumNameUniquenessCondition findCondition(long theaterId, String name);
}
