package com.movie.shop.api.operator.domain.aggregate.permission;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.movie.shop.api.operator.domain.exceptions.OperatorDomainException;
import com.movie.shop.api.operator.domain.policy.TheaterScopeCreationPolicy;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

        public static SingleTheater create(long theaterId, TheaterScopeCreationPolicy policy) {
            if (theaterId <= 0) {
                throw new OperatorDomainException("영화관 식별자는 0보다 커야 합니다.");
            }

            if (policy == null) {
                throw new OperatorDomainException("영화관 범위 생성 정책은 필수입니다.");
            }

            policy.validateCanCreateSingleTheater(theaterId);

            return new SingleTheater(theaterId);
        }
    }
}
