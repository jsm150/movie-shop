package com.movie.shop.api.theater.domain.port;

import com.movie.shop.api.theater.domain.aggregate.Theater;
import java.util.Optional;

public interface TheaterJpaPort {

    Theater save(Theater theater);

    Optional<Theater> findById(Long theaterId);

    void delete(Theater theater);

    long count();
}
