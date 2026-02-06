package com.movie.shop.api.screening.api.commands;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Voidy;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상영 상태 변경 요청")
public record ChangeStateScreeningCommand(
        @Schema(description = "상영 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        long screeningId,

        @Schema(description = "변경할 상태", example = "OPEN_SALES", requiredMode = Schema.RequiredMode.REQUIRED)
        ChangeState status,

        @Schema(description = "취소 사유(CANCEL 상태일 때 필수)", example = "상영 장비 점검")
        String cancelReason
) implements Command<Voidy> {

    @Schema(description = "상영 상태 변경 타입")
    public enum ChangeState {
        OPEN_SALES,
        CLOSE_SALES,
        CANCEL,
        FINISH
    }
}
