package com.movie.shop.api.theater.infrastructure.persistence;

import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.condition.TheaterNameUniquenessCondition;
import com.movie.shop.api.theater.domain.port.TheaterJpaPort;
import com.movie.shop.api.theater.domain.port.TheaterNameUniquenessConditionPort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TheaterJpaAdapter extends JpaRepository<Theater, Long>, TheaterJpaPort, TheaterNameUniquenessConditionPort {

    boolean existsByName_Name(String name);

    @Override
    default TheaterNameUniquenessCondition findCondition(String name) {
        return new TheaterNameUniquenessCondition(!existsByName_Name(name));
    }
}
