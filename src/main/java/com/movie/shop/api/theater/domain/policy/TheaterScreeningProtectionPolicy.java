package com.movie.shop.api.theater.domain.policy;

import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.aggregate.TheaterActiveChange;
import com.movie.shop.api.theater.domain.exceptions.TheaterDomainException;
import com.movie.shop.api.theater.domain.port.CheckTheaterScreeningLinkPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TheaterScreeningProtectionPolicy {

    private final CheckTheaterScreeningLinkPort checkTheaterScreeningLinkPort;

    public void validateCanChangeActive(Theater theater, TheaterActiveChange activeChange) {
        if (activeChange != TheaterActiveChange.DEACTIVATE) {
            return;
        }

        if (!theater.isActive()) {
            return;
        }

        if (checkTheaterScreeningLinkPort.existsBlockingScreeningByTheaterId(theater.getId())) {
            throw new TheaterDomainException("예정/판매중/판매종료 상영이 존재하는 극장은 비활성화할 수 없습니다.");
        }
    }

    public void validateCanDelete(Theater theater) {
        if (checkTheaterScreeningLinkPort.existsBlockingScreeningByTheaterId(theater.getId())) {
            throw new TheaterDomainException("예정/판매중/판매종료 상영이 존재하는 극장은 삭제할 수 없습니다.");
        }
    }
}
