package com.movie.shop.api.auditorium.api.commands;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Voidy;
import com.movie.shop.api.auditorium.domain.aggregate.AuditoriumStatusChange;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상영관 상태 변경 요청")
public record ChangeStatusAuditoriumCommand(
        @Schema(description = "상영관 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        long auditoriumId,

        @Schema(description = "변경할 상태", example = "DEACTIVATE", requiredMode = Schema.RequiredMode.REQUIRED)
        AuditoriumStatusChange status
) implements Command<Voidy> { }
