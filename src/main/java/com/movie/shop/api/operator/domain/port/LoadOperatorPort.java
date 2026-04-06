package com.movie.shop.api.operator.domain.port;

import com.movie.shop.api.operator.domain.aggregate.Operator;

public interface LoadOperatorPort {

    Operator getById(long operatorId);
}
