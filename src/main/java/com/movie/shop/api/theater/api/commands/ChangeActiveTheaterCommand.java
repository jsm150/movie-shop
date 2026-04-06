package com.movie.shop.api.theater.api.commands;

import an.awesome.pipelinr.Voidy;
import com.movie.shop.api.theater.api.authorization.TheaterManageCommand;
import com.movie.shop.api.theater.domain.aggregate.TheaterActiveChange;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "영화관 활성화/비활성화 요청")
public record ChangeActiveTheaterCommand(
        @Schema(description = "영화관 ID", example = "1")
        long theaterId,
        @Schema(description = "변경할 상태", example = "ACTIVATE")
        TheaterActiveChange status
) implements TheaterManageCommand<Voidy> {
}
