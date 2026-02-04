package com.movie.shop.api.theater.api.commands;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Voidy;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "극장 활성화/비활성화 요청")
public record ChangeActiveTheaterCommand(
        @Schema(description = "상영관 ID", example = "1")
        long theaterId,
        @Schema(description = "변경할 상태", example = "ACTIVATE")
        ChangeState status
) implements Command<Voidy> {

    @Schema(description = "극장 활성 상태")
    public enum ChangeState {
        @Schema(description = "활성화")
        ACTIVATE,
        @Schema(description = "비활성화")
        DEACTIVATE
    }
}
