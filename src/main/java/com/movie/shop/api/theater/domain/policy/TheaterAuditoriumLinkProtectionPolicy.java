package com.movie.shop.api.theater.domain.policy;

import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.aggregate.TheaterActiveChange;
import com.movie.shop.api.theater.domain.exceptions.TheaterDomainException;
import com.movie.shop.api.theater.domain.policy.status.TheaterAuditoriumLinkStatus;

import java.util.Objects;

public class TheaterAuditoriumLinkProtectionPolicy {

    private final TheaterAuditoriumLinkStatus theaterAuditoriumLinkStatus;

    public TheaterAuditoriumLinkProtectionPolicy(TheaterAuditoriumLinkStatus theaterAuditoriumLinkStatus) {
        this.theaterAuditoriumLinkStatus = Objects.requireNonNull(
                theaterAuditoriumLinkStatus,
                "영화관-상영관 연결 상태는 필수입니다."
        );
    }

    public void validateCanChangeActive(Theater theater, TheaterActiveChange activeChange) {
        if (activeChange != TheaterActiveChange.DEACTIVATE) {
            return;
        }

        if (!theater.isActive()) {
            return;
        }

        if (theaterAuditoriumLinkStatus.linked()) {
            throw new TheaterDomainException("연결된 상영관이 존재하는 영화관은 비활성화할 수 없습니다.");
        }
    }

    public void validateCanDelete(Theater theater) {
        if (theaterAuditoriumLinkStatus.linked()) {
            throw new TheaterDomainException("연결된 상영관이 존재하는 영화관은 삭제할 수 없습니다.");
        }
    }
}
