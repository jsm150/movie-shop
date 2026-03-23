package com.movie.shop.api.screening.infrastructure.persistence;

import com.movie.shop.api.screening.domain.aggregate.Screening;
import com.movie.shop.api.screening.domain.port.ScreeningJpaPort;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface ScreeningJpaAdapter extends JpaRepository<Screening, Long>, ScreeningJpaPort {

    @Override
    List<Screening> findAllByAuditoriumId(long auditoriumId);

    @Override
    @Query("""
            SELECT (COUNT(s) > 0)
            FROM Screening s
            WHERE s.movieId = :movieId
            """)
    boolean existsByMovieId(@Param("movieId") long movieId);

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT s
            FROM Screening s
            WHERE s.auditoriumId = :auditoriumId
              AND s.screeningTimeRange.startTime < :endTime
              AND :startTime < s.screeningTimeRange.endTime
            """)
    List<Screening> findConflictCandidatesByAuditoriumId(@Param("auditoriumId") long auditoriumId,
                                                       @Param("startTime") OffsetDateTime startTime,
                                                       @Param("endTime") OffsetDateTime endTime);

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT s
            FROM Screening s
            WHERE s.auditoriumId = :auditoriumId
              AND s.id <> :screeningId
              AND s.screeningTimeRange.startTime < :endTime
              AND :startTime < s.screeningTimeRange.endTime
            """)
    List<Screening> findConflictCandidatesByAuditoriumIdAndIdNot(@Param("auditoriumId") long auditoriumId,
                                                               @Param("startTime") OffsetDateTime startTime,
                                                               @Param("endTime") OffsetDateTime endTime,
                                                               @Param("screeningId") long screeningId);
}
