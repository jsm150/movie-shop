package com.movie.shop.api.screening.domain.aggregate.port;

import com.movie.shop.api.screening.domain.aggregate.Screening;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface ScreeningJpaPort {

    Screening save(Screening screening);

    Optional<Screening> findById(Long screeningId);

    void delete(Screening screening);

    boolean existsOverlappingByTheaterId(long theaterId,
                                         OffsetDateTime startTime,
                                         OffsetDateTime endTime);

    boolean existsOverlappingByTheaterIdAndIdNot(long theaterId,
                                                 OffsetDateTime startTime,
                                                 OffsetDateTime endTime,
                                                 long screeningId);
}
