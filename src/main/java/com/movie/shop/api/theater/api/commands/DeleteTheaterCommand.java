package com.movie.shop.api.theater.api.commands;

import an.awesome.pipelinr.Voidy;
import com.movie.shop.api.theater.api.authorization.TheaterManageCommand;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "영화관 삭제 요청")
public record DeleteTheaterCommand(
        @Schema(description = "영화관 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        long theaterId
) implements TheaterManageCommand<Voidy> {
}
