package com.movie.shop.api.auditorium.domain.port;

import com.movie.shop.api.auditorium.domain.aggregate.Auditorium;

import java.util.Optional;

public interface AuditoriumJpaPort {

    Auditorium save(Auditorium auditorium);

    Optional<Auditorium> findById(Long auditoriumId);

    void delete(Auditorium auditorium);

    long count();

    boolean existsByTheaterId(long theaterId);
}
