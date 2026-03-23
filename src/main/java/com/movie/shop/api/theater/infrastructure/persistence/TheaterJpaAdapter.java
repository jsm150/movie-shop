package com.movie.shop.api.theater.infrastructure.persistence;

import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.port.TheaterJpaPort;
import com.movie.shop.api.theater.domain.policy.status.TheaterNameDuplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TheaterJpaAdapter extends JpaRepository<Theater, Long>, TheaterJpaPort {

    boolean existsByName_Name(String name);

    @Override
    default boolean existsByName(String name) {
        return existsByName_Name(name);
    }

    @Override
    default TheaterNameDuplication loadNameDuplication(String name) {
        return new TheaterNameDuplication(existsByName_Name(name));
    }
}
