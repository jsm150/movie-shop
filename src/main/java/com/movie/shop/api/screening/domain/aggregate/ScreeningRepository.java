package com.movie.shop.api.screening.domain.aggregate;

import com.movie.shop.api.screening.domain.aggregate.port.ScreeningJpaPort;
import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScreeningRepository {

    private final ScreeningJpaPort screeningJpaPort;

    public Screening save(Screening screening) {
        return screeningJpaPort.save(screening);
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
