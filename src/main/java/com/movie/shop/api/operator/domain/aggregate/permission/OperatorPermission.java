package com.movie.shop.api.operator.domain.aggregate.permission;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.movie.shop.api.operator.domain.exceptions.OperatorDomainException;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "permissionType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OperatorPermission.MovieManagePermission.class, name = "MOVIE_MANAGE"),
        @JsonSubTypes.Type(value = OperatorPermission.OperatorManagePermission.class, name = "OPERATOR_MANAGE"),
        @JsonSubTypes.Type(value = OperatorPermission.TheaterManagePermission.class, name = "THEATER_MANAGE"),
        @JsonSubTypes.Type(value = OperatorPermission.AuditoriumManagePermission.class, name = "AUDITORIUM_MANAGE"),
        @JsonSubTypes.Type(value = OperatorPermission.ScreeningManagePermission.class, name = "SCREENING_MANAGE")
})
public sealed interface OperatorPermission
        permits OperatorPermission.MovieManagePermission,
        OperatorPermission.OperatorManagePermission,
        OperatorPermission.TheaterManagePermission,
        OperatorPermission.AuditoriumManagePermission,
        OperatorPermission.ScreeningManagePermission {

    record MovieManagePermission() implements OperatorPermission {
    }

    record OperatorManagePermission() implements OperatorPermission {
    }

    record TheaterManagePermission(TheaterPermissionScope scope) implements OperatorPermission {
        public TheaterManagePermission {
            validateScope(scope);
        }
    }

    record AuditoriumManagePermission(TheaterPermissionScope scope) implements OperatorPermission {
        public AuditoriumManagePermission {
            validateScope(scope);
        }
    }

    record ScreeningManagePermission(TheaterPermissionScope scope) implements OperatorPermission {
        public ScreeningManagePermission {
            validateScope(scope);
        }
    }

    private static void validateScope(TheaterPermissionScope scope) {
        if (scope == null) {
            throw new OperatorDomainException("영화관 범위는 필수입니다.");
        }
    }
}
