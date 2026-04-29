package com.movie.shop.api.screening.domain.port;

import com.movie.shop.api.screening.domain.aggregate.Screening;

import java.time.OffsetDateTime;
import java.util.List;

public interface ScreeningOverlapCandidatesPort {

    List<Screening> findOverlapCandidates(long auditoriumId, OffsetDateTime startTime, OffsetDateTime endTime);
}
