package com.movie.shop.api.screening.api.commands;

import an.awesome.pipelinr.Voidy;
import com.movie.shop.api.screening.api.authorization.ScreeningManageCommand;
import com.movie.shop.api.screening.domain.aggregate.ScreeningStateChange;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상영 상태 변경 요청")
public record ChangeStateScreeningCommand(
        @Schema(description = "상영 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        long screeningId,

        @Schema(description = "변경할 상태", example = "OPEN_SALES", requiredMode = Schema.RequiredMode.REQUIRED)
        ScreeningStateChange status,

        @Schema(description = "취소 사유(CANCEL 상태일 때 필수)", example = "상영 장비 점검")
        String cancelReason
) implements ScreeningManageCommand<Voidy> {
}
