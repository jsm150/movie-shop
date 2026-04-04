package com.movie.shop.api.screening.infrastructure.policy;

import com.movie.shop.api.auditorium.domain.port.AuditoriumJpaPort;
import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;
import com.movie.shop.api.screening.domain.port.LoadScreeningTheaterIdPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoadScreeningTheaterIdJpaAdapter implements LoadScreeningTheaterIdPort {

    private final AuditoriumJpaPort auditoriumJpaPort;

    @Override
    public long loadTheaterId(long auditoriumId) {
        return auditoriumJpaPort.findById(auditoriumId)
                .map(auditorium -> auditorium.getTheaterId())
                .orElseThrow(() -> new ScreeningDomainException("상영관 정보를 찾을 수 없습니다."));
    }
}
