package com.movie.shop.api.screening.infrastructure.policy;

import com.movie.shop.api.screening.domain.aggregate.ScreeningRepository;
import com.movie.shop.api.screening.domain.policy.port.CheckScreeningTimeConflictPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class CheckScreeningTimeConflictJpaAdapter implements CheckScreeningTimeConflictPort {

    private final ScreeningRepository screeningRepository;

    @Override
    public boolean hasConflict(long theaterId, OffsetDateTime screeningStart, OffsetDateTime screeningEnd) {
        return screeningRepository.existsOverlappingByTheaterId(theaterId, screeningStart, screeningEnd);
    }

    @Override
    public boolean hasConflictExcluding(long screeningId,
                                        long theaterId,
                                        OffsetDateTime screeningStart,
                                        OffsetDateTime screeningEnd) {
        return screeningRepository.existsOverlappingByTheaterIdAndIdNot(
                theaterId,
                screeningStart,
                screeningEnd,
                screeningId
        );
    }
}
