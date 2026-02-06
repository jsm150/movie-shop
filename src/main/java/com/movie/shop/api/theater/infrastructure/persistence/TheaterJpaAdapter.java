package com.movie.shop.api.theater.infrastructure.persistence;

import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.aggregate.port.TheaterJpaPort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TheaterJpaAdapter extends JpaRepository<Theater, Long>, TheaterJpaPort {

    boolean existsByName_Name(String name);

    @Override
    default boolean existsByName(String name) {
        return existsByName_Name(name);
    }
}
