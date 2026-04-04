package com.movie.shop.api.operator.domain.aggregate.permission;

import java.util.List;

import com.movie.shop.api.operator.domain.exceptions.OperatorDomainException;

public sealed interface OperatorAuthorizationRequirement
        permits OperatorAuthorizationRequirement.RequireMovieManage,
        OperatorAuthorizationRequirement.RequireOperatorManage,
        OperatorAuthorizationRequirement.RequireTheaterManage,
        OperatorAuthorizationRequirement.RequireAuditoriumManage,
        OperatorAuthorizationRequirement.RequireScreeningManage {

    boolean isSatisfiedBy(List<OperatorPermission> permissions);

    record RequireMovieManage() implements OperatorAuthorizationRequirement {
        @Override
        public boolean isSatisfiedBy(List<OperatorPermission> permissions) {
            return hasPermission(permissions, OperatorPermission.MovieManagePermission.class);
        }
    }

    record RequireOperatorManage() implements OperatorAuthorizationRequirement {
        @Override
        public boolean isSatisfiedBy(List<OperatorPermission> permissions) {
            return hasPermission(permissions, OperatorPermission.OperatorManagePermission.class);
        }
    }

    record RequireTheaterManage(TheaterRequirementScope scope) implements OperatorAuthorizationRequirement {
        public RequireTheaterManage {
            validateScope(scope);
        }

        @Override
        public boolean isSatisfiedBy(List<OperatorPermission> permissions) {
            validatePermissions(permissions);
            return permissions.stream()
                    .anyMatch(permission -> permission instanceof OperatorPermission.TheaterManagePermission theaterManage
                            && scope.isSatisfiedBy(theaterManage.scope()));
        }
    }

    record RequireAuditoriumManage(TheaterRequirementScope scope) implements OperatorAuthorizationRequirement {
        public RequireAuditoriumManage {
            validateScope(scope);
        }

        @Override
        public boolean isSatisfiedBy(List<OperatorPermission> permissions) {
            validatePermissions(permissions);
            return permissions.stream()
                    .anyMatch(permission -> permission instanceof OperatorPermission.AuditoriumManagePermission auditoriumManage
                            && scope.isSatisfiedBy(auditoriumManage.scope()));
        }
    }

    record RequireScreeningManage(TheaterRequirementScope scope) implements OperatorAuthorizationRequirement {
        public RequireScreeningManage {
            validateScope(scope);
        }

        @Override
        public boolean isSatisfiedBy(List<OperatorPermission> permissions) {
            validatePermissions(permissions);
            return permissions.stream()
                    .anyMatch(permission -> permission instanceof OperatorPermission.ScreeningManagePermission screeningManage
                            && scope.isSatisfiedBy(screeningManage.scope()));
        }
    }

    private static <P extends OperatorPermission> boolean hasPermission(
            List<OperatorPermission> permissions,
            Class<P> permissionType
    ) {
        validatePermissions(permissions);
        return permissions.stream().anyMatch(permissionType::isInstance);
    }

    private static void validateScope(TheaterRequirementScope scope) {
        if (scope == null) {
            throw new OperatorDomainException("인가 요구 영화관 범위는 필수입니다.");
        }
    }

    private static void validatePermissions(List<OperatorPermission> permissions) {
        if (permissions == null) {
            throw new OperatorDomainException("운영자 권한 목록은 필수입니다.");
        }
    }
}
