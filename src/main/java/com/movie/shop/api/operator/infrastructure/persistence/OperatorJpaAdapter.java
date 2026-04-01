package com.movie.shop.api.operator.infrastructure.persistence;

import com.movie.shop.api.operator.domain.aggregate.Operator;
import com.movie.shop.api.operator.domain.port.OperatorJpaPort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OperatorJpaAdapter extends JpaRepository<Operator, Long>, OperatorJpaPort {

    @Override
    Optional<Operator> findByLoginId(String loginId);

    @Override
    boolean existsByLoginId(String loginId);
}
