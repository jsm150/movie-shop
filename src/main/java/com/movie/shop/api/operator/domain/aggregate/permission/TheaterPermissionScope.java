package com.movie.shop.api.operator.domain.aggregate.permission;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.movie.shop.api.operator.domain.condition.OperatorTheaterPermissionScopeTarget;
import com.movie.shop.api.operator.domain.exceptions.OperatorDomainException;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "scopeType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TheaterPermissionScope.AllTheaters.class, name = "ALL_THEATERS"),
        @JsonSubTypes.Type(value = TheaterPermissionScope.SingleTheater.class, name = "SINGLE_THEATER")
})
public sealed interface TheaterPermissionScope
        permits TheaterPermissionScope.AllTheaters, TheaterPermissionScope.SingleTheater {

    record AllTheaters() implements TheaterPermissionScope {
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @EqualsAndHashCode
    final class SingleTheater implements TheaterPermissionScope {

        private long theaterId;

        private SingleTheater(long theaterId) {
            this.theaterId = theaterId;
        }

        public static SingleTheater create(Optional<OperatorTheaterPermissionScopeTarget> scopeTarget) {
            if (scopeTarget == null) {
                throw new OperatorDomainException("영화관 권한 범위 대상은 필수입니다.");
            }

            OperatorTheaterPermissionScopeTarget resolvedTarget = scopeTarget.orElseThrow(
                    () -> new OperatorDomainException("존재하지 않는 영화관으로 권한 범위를 생성할 수 없습니다.")
            );

            return new SingleTheater(resolvedTarget.theaterId());
        }
    }
}
