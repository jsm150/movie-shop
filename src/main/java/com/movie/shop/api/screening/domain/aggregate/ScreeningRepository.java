package com.movie.shop.api.screening.domain.aggregate;

import com.movie.shop.api.screening.domain.aggregate.port.ScreeningJpaPort;
import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class ScreeningRepository {

    private final ScreeningJpaPort screeningJpaPort;

    public Screening save(Screening screening) {
        return screeningJpaPort.save(screening);
    }

    public boolean existsOverlappingByTheaterId(long theaterId,
                                                OffsetDateTime startTime,
                                                OffsetDateTime endTime) {
        return screeningJpaPort.existsOverlappingByTheaterId(theaterId, startTime, endTime);
    }

    public boolean existsOverlappingByTheaterIdAndIdNot(long theaterId,
                                                        OffsetDateTime startTime,
                                                        OffsetDateTime endTime,
                                                        long screeningId) {
        return screeningJpaPort.existsOverlappingByTheaterIdAndIdNot(theaterId, startTime, endTime, screeningId);
    }

    public void removeScheduledById(long screeningId) {
        Screening screening = getById(screeningId);
        screening.validateCanRemove();
        screeningJpaPort.delete(screening);
    }

    public Screening getById(long screeningId) {
        return screeningJpaPort.findById(screeningId)
                .orElseThrow(() -> new ScreeningDomainException("상영 정보를 찾을 수 없습니다."));
    }
}
