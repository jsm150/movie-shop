package com.movie.shop.api.screening.infrastructure.persistence;

import com.movie.shop.api.screening.domain.aggregate.Screening;
import com.movie.shop.api.screening.domain.aggregate.port.ScreeningJpaPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

public interface ScreeningJpaAdapter extends JpaRepository<Screening, Long>, ScreeningJpaPort {

    @Override
    @Query("""
            SELECT (COUNT(s) > 0)
            FROM Screening s
            WHERE s.movieId = :movieId
            """)
    boolean existsByMovieId(@Param("movieId") long movieId);

    @Override
    @Query("""
            SELECT (COUNT(s) > 0)
            FROM Screening s
            WHERE s.theaterId = :theaterId
              AND s.screeningTimeRange.startTime < :endTime
              AND :startTime < s.screeningTimeRange.endTime
            """)
    boolean existsOverlappingByTheaterId(@Param("theaterId") long theaterId,
                                         @Param("startTime") OffsetDateTime startTime,
                                         @Param("endTime") OffsetDateTime endTime);

    @Override
    @Query("""
            SELECT (COUNT(s) > 0)
            FROM Screening s
            WHERE s.theaterId = :theaterId
              AND s.id <> :screeningId
              AND s.screeningTimeRange.startTime < :endTime
              AND :startTime < s.screeningTimeRange.endTime
            """)
    boolean existsOverlappingByTheaterIdAndIdNot(@Param("theaterId") long theaterId,
                                                 @Param("startTime") OffsetDateTime startTime,
                                                 @Param("endTime") OffsetDateTime endTime,
                                                 @Param("screeningId") long screeningId);
}
