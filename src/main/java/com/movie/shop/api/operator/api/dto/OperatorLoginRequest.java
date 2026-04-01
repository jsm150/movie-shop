package com.movie.shop.api.operator.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 요청")
public record OperatorLoginRequest(
        @Schema(description = "로그인 ID", example = "test-admin", requiredMode = Schema.RequiredMode.REQUIRED)
        String loginId,

        @Schema(description = "비밀번호", example = "1234", requiredMode = Schema.RequiredMode.REQUIRED)
        String password
) {
}
