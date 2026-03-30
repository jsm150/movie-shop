package com.movie.shop.api.theater.domain.policy;

import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.aggregate.TheaterActiveChange;
import com.movie.shop.api.theater.domain.exceptions.TheaterDomainException;
import com.movie.shop.api.theater.domain.port.CheckTheaterAuditoriumLinkPort;

import java.util.Objects;

public class TheaterAuditoriumLinkProtectionPolicy {

    private final CheckTheaterAuditoriumLinkPort checkTheaterAuditoriumLinkPort;

    public TheaterAuditoriumLinkProtectionPolicy(CheckTheaterAuditoriumLinkPort checkTheaterAuditoriumLinkPort) {
        this.checkTheaterAuditoriumLinkPort = Objects.requireNonNull(
                checkTheaterAuditoriumLinkPort,
                "영화관-상영관 연결 조회 포트가 필수입니다."
        );
    }

    public void validateCanChangeActive(Theater theater, TheaterActiveChange activeChange) {
        if (activeChange != TheaterActiveChange.DEACTIVATE) {
            return;
        }

        if (!theater.isActive()) {
            return;
        }

        if (checkTheaterAuditoriumLinkPort.loadTheaterAuditoriumLinkStatus(theater.getId())) {
            throw new TheaterDomainException("상영관이 연결된 영화관은 비활성화할 수 없습니다.");
        }
    }

    public void validateCanDelete(Theater theater) {
        if (checkTheaterAuditoriumLinkPort.loadTheaterAuditoriumLinkStatus(theater.getId())) {
            throw new TheaterDomainException("상영관이 연결된 영화관은 삭제할 수 없습니다.");
        }
    }
}
