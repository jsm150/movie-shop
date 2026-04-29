package com.movie.shop.api.screening.domain.port;

import java.util.Optional;

import com.movie.shop.api.screening.domain.condition.MovieSchedulingCondition;

public interface MovieSchedulingConditionPort {
    Optional<MovieSchedulingCondition> findCondition(long movieId);
}
