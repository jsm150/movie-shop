package com.movie.shop.api.operator.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.movie.shop.api.operator.domain.aggregate.Operator;
import com.movie.shop.api.operator.domain.port.OperatorJpaPort;

public interface OperatorJpaAdapter extends JpaRepository<Operator, Long>, OperatorJpaPort {

    @Override
    @EntityGraph(attributePaths = "permissions")
    Optional<Operator> findById(Long operatorId);

    @Override
    @EntityGraph(attributePaths = "permissions")
    Optional<Operator> findByLoginId(String loginId);

    @Override
    boolean existsByLoginId(String loginId);
}
