package com.movie.shop.api.theater.domain.port;

import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.policy.status.TheaterNameDuplication;

import java.util.Optional;

public interface TheaterJpaPort {

    Theater save(Theater theater);

    Optional<Theater> findById(Long theaterId);

    void delete(Theater theater);

    long count();

    boolean existsByName(String name);

    TheaterNameDuplication loadNameDuplication(String name);
}
