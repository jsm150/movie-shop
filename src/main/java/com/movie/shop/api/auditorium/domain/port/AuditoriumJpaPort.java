package com.movie.shop.api.auditorium.domain.port;

import com.movie.shop.api.auditorium.domain.aggregate.Auditorium;
import com.movie.shop.api.auditorium.domain.policy.status.AuditoriumNameDuplication;

import java.util.Optional;

public interface AuditoriumJpaPort {

    Auditorium save(Auditorium auditorium);

    Optional<Auditorium> findById(Long auditoriumId);

    void delete(Auditorium auditorium);

    long count();

    boolean existsByTheaterId(long theaterId);

    boolean existsByTheaterIdAndName(long theaterId, String name);

    AuditoriumNameDuplication loadNameDuplication(long theaterId, String name);
}
