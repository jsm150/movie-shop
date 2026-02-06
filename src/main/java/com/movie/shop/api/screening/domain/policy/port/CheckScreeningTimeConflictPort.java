package com.movie.shop.api.screening.domain.policy.port;

import java.time.OffsetDateTime;

public interface CheckScreeningTimeConflictPort {
    boolean hasConflict(long theaterId, OffsetDateTime screeningStart, OffsetDateTime screeningEnd);

    boolean hasConflictExcluding(long screeningId, long theaterId, OffsetDateTime screeningStart, OffsetDateTime screeningEnd);
}
