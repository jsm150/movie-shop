package com.movie.shop.api.theater.domain.policy;

import com.movie.shop.api.theater.domain.exceptions.TheaterDomainException;
import com.movie.shop.api.theater.domain.policy.status.TheaterNameDuplication;

import java.util.Objects;

public class TheaterNamePolicy {

    private final TheaterNameDuplication nameDuplication;

    public TheaterNamePolicy(TheaterNameDuplication nameDuplication) {
        this.nameDuplication = Objects.requireNonNull(nameDuplication, "영화관 이름 중복 정보는 필수입니다.");
    }

    public void validateNotDuplicate() {
        if (nameDuplication.duplicated()) {
            throw new TheaterDomainException("동일한 이름의 영화관이 이미 존재합니다.");
        }
    }
}
