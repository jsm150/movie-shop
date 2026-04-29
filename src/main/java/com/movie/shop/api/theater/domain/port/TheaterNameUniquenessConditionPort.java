package com.movie.shop.api.theater.domain.port;

import com.movie.shop.api.theater.domain.condition.TheaterNameUniquenessCondition;

public interface TheaterNameUniquenessConditionPort {
    TheaterNameUniquenessCondition findCondition(String name);
}
