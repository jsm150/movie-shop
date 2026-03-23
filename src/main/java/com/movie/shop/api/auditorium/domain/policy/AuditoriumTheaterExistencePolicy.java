package com.movie.shop.api.auditorium.domain.policy;

import com.movie.shop.api.auditorium.domain.exceptions.AuditoriumDomainException;
import com.movie.shop.api.auditorium.domain.policy.status.AuditoriumTheaterExistenceStatus;

import java.util.Objects;

public class AuditoriumTheaterExistencePolicy {

    private final AuditoriumTheaterExistenceStatus theaterExistenceStatus;

    public AuditoriumTheaterExistencePolicy(AuditoriumTheaterExistenceStatus theaterExistenceStatus) {
        this.theaterExistenceStatus = Objects.requireNonNull(
                theaterExistenceStatus,
                "영화관 존재 상태 정보는 필수입니다."
        );
    }

    public void validateCanRegister() {
        if (!theaterExistenceStatus.exists()) {
            throw new AuditoriumDomainException("존재하지 않는 영화관에는 상영관을 등록할 수 없습니다.");
        }
    }
}
