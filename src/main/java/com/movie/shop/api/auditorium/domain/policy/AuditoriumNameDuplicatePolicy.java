package com.movie.shop.api.auditorium.domain.policy;

import com.movie.shop.api.auditorium.domain.exceptions.AuditoriumDomainException;
import com.movie.shop.api.auditorium.domain.policy.status.AuditoriumNameDuplication;

import java.util.Objects;

public class AuditoriumNameDuplicatePolicy {

    private final AuditoriumNameDuplication nameDuplication;

    public AuditoriumNameDuplicatePolicy(AuditoriumNameDuplication nameDuplication) {
        this.nameDuplication = Objects.requireNonNull(nameDuplication, "상영관 이름 중복 정보는 필수입니다.");
    }

    public void validateNotDuplicate() {
        if (nameDuplication.duplicated()) {
            throw new AuditoriumDomainException("동일한 이름의 상영관이 해당 영화관에 이미 존재합니다.");
        }
    }
}
