package com.movie.shop.api.screening.api.commands;

import an.awesome.pipelinr.Voidy;
import com.movie.shop.api.screening.api.authorization.ScreeningManageCommand;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상영 삭제 요청")
public record DeleteScreeningCommand(
        @Schema(description = "상영 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        long screeningId
) implements ScreeningManageCommand<Voidy> {
}
