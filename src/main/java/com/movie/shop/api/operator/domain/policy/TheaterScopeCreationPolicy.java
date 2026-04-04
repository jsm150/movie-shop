package com.movie.shop.api.operator.domain.policy;

import java.util.Objects;

import com.movie.shop.api.operator.domain.exceptions.OperatorDomainException;
import com.movie.shop.api.operator.domain.port.CheckOperatorTheaterExistencePort;

public class TheaterScopeCreationPolicy {

    private final CheckOperatorTheaterExistencePort checkOperatorTheaterExistencePort;

    public TheaterScopeCreationPolicy(CheckOperatorTheaterExistencePort checkOperatorTheaterExistencePort) {
        this.checkOperatorTheaterExistencePort = Objects.requireNonNull(
                checkOperatorTheaterExistencePort,
                "영화관 존재 확인 포트는 필수입니다."
        );
    }

    public void validateCanCreateSingleTheater(long theaterId) {
        if (!checkOperatorTheaterExistencePort.existsTheater(theaterId)) {
            throw new OperatorDomainException("존재하지 않는 영화관으로 권한 범위를 생성할 수 없습니다.");
        }
    }
}
