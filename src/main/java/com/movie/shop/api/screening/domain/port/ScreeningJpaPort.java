package com.movie.shop.api.screening.domain.port;

import com.movie.shop.api.screening.domain.aggregate.Screening;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ScreeningJpaPort {

    Screening save(Screening screening);

    Optional<Screening> findById(Long screeningId);

    void delete(Screening screening);

    List<Screening> findAllByAuditoriumId(long auditoriumId);

    boolean existsByMovieId(long movieId);

    List<Screening> findOverlapCandidatesByAuditoriumId(long auditoriumId,
                                                        OffsetDateTime startTime,
                                                        OffsetDateTime endTime);
}
