package com.movie.shop.api.auditorium.infrastructure.persistence;

import com.movie.shop.api.auditorium.domain.aggregate.Auditorium;
import com.movie.shop.api.auditorium.domain.policy.status.AuditoriumNameDuplication;
import com.movie.shop.api.auditorium.domain.port.AuditoriumJpaPort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriumJpaAdapter extends JpaRepository<Auditorium, Long>, AuditoriumJpaPort {

    @Override
    boolean existsByTheaterId(long theaterId);

    boolean existsByTheaterIdAndName_Name(long theaterId, String name);

    @Override
    default boolean existsByTheaterIdAndName(long theaterId, String name) {
        return existsByTheaterIdAndName_Name(theaterId, name);
    }

    @Override
    default AuditoriumNameDuplication loadNameDuplication(long theaterId, String name) {
        return new AuditoriumNameDuplication(existsByTheaterIdAndName_Name(theaterId, name));
    }
}
