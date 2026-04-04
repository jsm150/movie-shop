package com.movie.shop.api.operator.api.response;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답")
public record OperatorLoginResponse(
        @Schema(description = "JWT access token")
        String accessToken,

        @Schema(description = "토큰 타입", example = "Bearer")
        String tokenType,

        @Schema(description = "토큰 만료 시각")
        Instant expiresAt,

        @Schema(description = "현재 로그인한 운영자 정보")
        CurrentOperatorResponse operator
) {
}
