package com.movie.shop.api.auditorium.domain.policy;

import com.movie.shop.api.auditorium.domain.exceptions.AuditoriumDomainException;
import com.movie.shop.api.auditorium.domain.port.AuditoriumJpaPort;

import java.util.Objects;

public class AuditoriumNameDuplicatePolicy {

    private final AuditoriumJpaPort auditoriumJpaPort;

    public AuditoriumNameDuplicatePolicy(AuditoriumJpaPort auditoriumJpaPort) {
        this.auditoriumJpaPort = Objects.requireNonNull(auditoriumJpaPort, "상영관 이름 중복 조회 포트가 필수입니다.");
    }

    public void validateNotDuplicate(long theaterId, String name) {
        if (auditoriumJpaPort.loadNameDuplication(theaterId, name)) {
            throw new AuditoriumDomainException("동일한 이름의 상영관이 해당 영화관에 이미 존재합니다.");
        }
    }
}
