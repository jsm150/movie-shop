package com.movie.shop.api.auditorium.api.commands;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Voidy;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상영관 삭제 요청")
public record DeleteAuditoriumCommand(
        @Schema(description = "상영관 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        long auditoriumId
) implements Command<Voidy> { }
