package com.movie.shop.api.movie.api.commands;

import an.awesome.pipelinr.Voidy;
import com.movie.shop.api.movie.api.authorization.MovieManageCommand;
import com.movie.shop.api.movie.domain.aggregate.MovieStateChange;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "영화 상태 변경 요청")
public record ChangeStateMovieCommand(
        @Schema(description = "영화 ID", example = "1")
        long movieId,
        @Schema(description = "변경할 상태", example = "NOW_SHOWING")
        MovieStateChange status
) implements MovieManageCommand<Voidy> {
}
