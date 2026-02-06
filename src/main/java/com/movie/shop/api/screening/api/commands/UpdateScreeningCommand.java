package com.movie.shop.api.screening.api.commands;

import an.awesome.pipelinr.Command;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "상영 수정 요청")
public record UpdateScreeningCommand(
        @Schema(description = "상영 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        long screeningId,

        @Schema(description = "상영 시작 시간", example = "2026-02-06T13:00:00+09:00", requiredMode = Schema.RequiredMode.REQUIRED)
        OffsetDateTime screeningStartTime,

        @Schema(description = "상영 종료 시간", example = "2026-02-06T15:00:00+09:00", requiredMode = Schema.RequiredMode.REQUIRED)
        OffsetDateTime screeningEndTime,

        @Schema(description = "판매 시작 시간", example = "2026-02-02T10:00:00+09:00", requiredMode = Schema.RequiredMode.REQUIRED)
        OffsetDateTime salesStartAt,

        @Schema(description = "판매 종료 시간", example = "2026-02-06T13:00:00+09:00", requiredMode = Schema.RequiredMode.REQUIRED)
        OffsetDateTime salesEndAt
) implements Command<Long> {
}
