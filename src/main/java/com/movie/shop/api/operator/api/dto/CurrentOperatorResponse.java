package com.movie.shop.api.operator.api.dto;

import com.movie.shop.api.operator.api.application.AuthenticatedOperatorPrincipal;
import com.movie.shop.api.operator.domain.aggregate.OperatorStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "현재 로그인한 운영자 정보")
public record CurrentOperatorResponse(
        @Schema(description = "운영자 ID", example = "1")
        long operatorId,

        @Schema(description = "로그인 ID", example = "admin")
        String loginId,

        @Schema(description = "표시 이름", example = "Default Operator")
        String displayName,

        @Schema(description = "운영자 상태", example = "ACTIVE")
        OperatorStatus status
) {
    public static CurrentOperatorResponse from(AuthenticatedOperatorPrincipal principal) {
        return new CurrentOperatorResponse(
                principal.getOperatorId(),
                principal.getLoginId(),
                principal.getDisplayName(),
                principal.getStatus()
        );
    }
}
