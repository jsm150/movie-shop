package com.movie.shop.api.screening.domain.port;

import com.movie.shop.api.screening.domain.aggregate.Screening;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ScreeningJpaPort {

    Screening save(Screening screening);

    Optional<Screening> findById(Long screeningId);

    void delete(Screening screening);

    List<Screening> findAllByTheaterId(long theaterId);

    boolean existsByMovieId(long movieId);

    List<Screening> findConflictCandidatesByTheaterId(long theaterId,
                                                      OffsetDateTime startTime,
                                                      OffsetDateTime endTime);

    List<Screening> findConflictCandidatesByTheaterIdAndIdNot(long theaterId,
                                                              OffsetDateTime startTime,
                                                              OffsetDateTime endTime,
                                                              long screeningId);
}
