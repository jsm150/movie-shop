package com.movie.shop.api.operator.domain.port;

import com.movie.shop.api.operator.domain.aggregate.Operator;

import java.util.Optional;

public interface OperatorJpaPort {

    Operator save(Operator operator);

    Optional<Operator> findById(Long operatorId);

    Optional<Operator> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);
}
