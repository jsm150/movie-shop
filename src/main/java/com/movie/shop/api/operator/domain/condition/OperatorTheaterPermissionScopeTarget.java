package com.movie.shop.api.operator.domain.condition;

import com.movie.shop.api.operator.domain.exceptions.OperatorDomainException;

public record OperatorTheaterPermissionScopeTarget(long theaterId) {
    public OperatorTheaterPermissionScopeTarget {
        if (theaterId <= 0) {
            throw new OperatorDomainException("영화관 식별자는 0보다 커야 합니다.");
        }
    }
}
