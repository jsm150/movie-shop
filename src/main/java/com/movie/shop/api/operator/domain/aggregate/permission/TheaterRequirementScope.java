package com.movie.shop.api.operator.domain.aggregate.permission;

import com.movie.shop.api.operator.domain.exceptions.OperatorDomainException;

public sealed interface TheaterRequirementScope
        permits TheaterRequirementScope.AllTheaters, TheaterRequirementScope.SingleTheater {

    boolean isSatisfiedBy(TheaterPermissionScope permissionScope);

    record AllTheaters() implements TheaterRequirementScope {
        @Override
        public boolean isSatisfiedBy(TheaterPermissionScope permissionScope) {
            validatePermissionScope(permissionScope);
            return permissionScope instanceof TheaterPermissionScope.AllTheaters;
        }
    }

    record SingleTheater(long theaterId) implements TheaterRequirementScope {
        public SingleTheater {
            if (theaterId <= 0) {
                throw new OperatorDomainException("영화관 식별자는 0보다 커야 합니다.");
            }
        }

        @Override
        public boolean isSatisfiedBy(TheaterPermissionScope permissionScope) {
            validatePermissionScope(permissionScope);
            return permissionScope instanceof TheaterPermissionScope.AllTheaters
                    || permissionScope instanceof TheaterPermissionScope.SingleTheater singleTheater
                    && singleTheater.getTheaterId() == theaterId;
        }
    }

    private static void validatePermissionScope(TheaterPermissionScope permissionScope) {
        if (permissionScope == null) {
            throw new OperatorDomainException("영화관 범위는 필수입니다.");
        }
    }
}
