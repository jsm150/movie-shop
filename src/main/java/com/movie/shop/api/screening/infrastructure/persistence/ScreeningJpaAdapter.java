package com.movie.shop.api.screening.infrastructure.persistence;

import com.movie.shop.api.screening.domain.aggregate.Screening;
import com.movie.shop.api.screening.domain.port.ScreeningJpaPort;
import com.movie.shop.api.screening.domain.port.ScreeningOverlapCandidatesPort;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface ScreeningJpaAdapter extends JpaRepository<Screening, Long>, ScreeningJpaPort, ScreeningOverlapCandidatesPort {

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
    List<Screening> findOverlapCandidatesByAuditoriumId(@Param("auditoriumId") long auditoriumId,
                                                        @Param("startTime") OffsetDateTime startTime,
                                                        @Param("endTime") OffsetDateTime endTime);

    @Override
    default List<Screening> findOverlapCandidates(long auditoriumId, OffsetDateTime startTime, OffsetDateTime endTime) {
        return findOverlapCandidatesByAuditoriumId(auditoriumId, startTime, endTime);
    }
}
