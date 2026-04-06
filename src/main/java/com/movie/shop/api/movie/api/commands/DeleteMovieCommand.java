package com.movie.shop.api.movie.api.commands;

import an.awesome.pipelinr.Voidy;
import com.movie.shop.api.movie.api.authorization.MovieManageCommand;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "영화 삭제 요청")
public record DeleteMovieCommand(
        @Schema(description = "영화 번호", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        long movieId
) implements MovieManageCommand<Voidy> {
}
