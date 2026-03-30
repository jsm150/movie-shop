package com.movie.shop.api.theater.domain.policy;

import com.movie.shop.api.theater.domain.exceptions.TheaterDomainException;
import com.movie.shop.api.theater.domain.port.TheaterJpaPort;

import java.util.Objects;

public class TheaterNamePolicy {

    private final TheaterJpaPort theaterJpaPort;

    public TheaterNamePolicy(TheaterJpaPort theaterJpaPort) {
        this.theaterJpaPort = Objects.requireNonNull(theaterJpaPort, "영화관 이름 중복 조회 포트가 필수입니다.");
    }

    public void validateNotDuplicate(String name) {
        if (theaterJpaPort.loadNameDuplication(name)) {
            throw new TheaterDomainException("동일한 이름의 영화관이 이미 존재합니다.");
        }
    }
}
