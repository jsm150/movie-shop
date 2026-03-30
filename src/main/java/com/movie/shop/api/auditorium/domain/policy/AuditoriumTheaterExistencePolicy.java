package com.movie.shop.api.auditorium.domain.policy;

import com.movie.shop.api.auditorium.domain.exceptions.AuditoriumDomainException;
import com.movie.shop.api.auditorium.domain.port.LoadAuditoriumTheaterExistenceStatusPort;

import java.util.Objects;

public class AuditoriumTheaterExistencePolicy {

    private final LoadAuditoriumTheaterExistenceStatusPort loadAuditoriumTheaterExistenceStatusPort;

    public AuditoriumTheaterExistencePolicy(LoadAuditoriumTheaterExistenceStatusPort loadAuditoriumTheaterExistenceStatusPort) {
        this.loadAuditoriumTheaterExistenceStatusPort = Objects.requireNonNull(
                loadAuditoriumTheaterExistenceStatusPort,
                "영화관 존재 조회 포트는 필수입니다."
        );
    }

    public void validateCanRegister(long theaterId) {
        if (!loadAuditoriumTheaterExistenceStatusPort.loadAuditoriumTheaterExistenceStatus(theaterId)) {
            throw new AuditoriumDomainException("존재하지 않는 영화관에는 상영관을 등록할 수 없습니다.");
        }
    }
}
