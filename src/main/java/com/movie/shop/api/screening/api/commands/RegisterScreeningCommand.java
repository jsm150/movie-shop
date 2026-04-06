package com.movie.shop.api.screening.api.commands;

import com.movie.shop.api.screening.api.authorization.ScreeningManageCommand;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "상영 등록 요청")
public record RegisterScreeningCommand(
        @Schema(description = "영화 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        long movieId,

        @Schema(description = "상영관 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        long auditoriumId,

        @Schema(description = "상영 시작 시간", example = "2026-02-06T10:00:00+09:00", requiredMode = Schema.RequiredMode.REQUIRED)
        OffsetDateTime screeningStartTime,

        @Schema(description = "상영 종료 시간", example = "2026-02-06T12:00:00+09:00", requiredMode = Schema.RequiredMode.REQUIRED)
        OffsetDateTime screeningEndTime,

        @Schema(description = "판매 시작 시간", example = "2026-02-01T10:00:00+09:00", requiredMode = Schema.RequiredMode.REQUIRED)
        OffsetDateTime salesStartAt,

        @Schema(description = "판매 종료 시간", example = "2026-02-06T10:00:00+09:00", requiredMode = Schema.RequiredMode.REQUIRED)
        OffsetDateTime salesEndAt
) implements ScreeningManageCommand<Long> {
}
