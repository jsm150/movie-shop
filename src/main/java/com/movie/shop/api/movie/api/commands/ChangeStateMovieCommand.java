package com.movie.shop.api.movie.api.commands;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Voidy;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "영화 상태 변경 요청")
public record ChangeStateMovieCommand(
        @Schema(description = "영화 ID", example = "1")
        long movieId,
        @Schema(description = "변경할 상태", example = "NOW_SHOWING")
        ChangeState status
) implements Command<Voidy> {
    @Schema(description = "영화 상태")
    public enum ChangeState {
        @Schema(description = "개봉 예정")
        COMING_SOON,
        @Schema(description = "상영 중")
        NOW_SHOWING,
        @Schema(description = "상영 종료")
        ENDED
    }
}
