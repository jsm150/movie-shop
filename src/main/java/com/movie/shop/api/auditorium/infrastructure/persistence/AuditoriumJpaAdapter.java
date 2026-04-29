package com.movie.shop.api.auditorium.infrastructure.persistence;

import com.movie.shop.api.auditorium.domain.aggregate.Auditorium;
import com.movie.shop.api.auditorium.domain.condition.AuditoriumNameUniquenessCondition;
import com.movie.shop.api.auditorium.domain.port.AuditoriumJpaPort;
import com.movie.shop.api.auditorium.domain.port.AuditoriumNameUniquenessConditionPort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriumJpaAdapter extends JpaRepository<Auditorium, Long>,
        AuditoriumJpaPort,
        AuditoriumNameUniquenessConditionPort {

    @Override
    boolean existsByTheaterId(long theaterId);

    boolean existsByTheaterIdAndName_Name(long theaterId, String name);

    @Override
    default AuditoriumNameUniquenessCondition findCondition(long theaterId, String name) {
        return new AuditoriumNameUniquenessCondition(!existsByTheaterIdAndName_Name(theaterId, name));
    }
}
