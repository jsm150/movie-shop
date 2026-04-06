package com.movie.shop.api.theater.api.commands;

import com.movie.shop.api.theater.api.authorization.TheaterManageCommand;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "영화관 수정 요청")
public record UpdateTheaterCommand(
        @Schema(description = "영화관 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        long theaterId,

        @Schema(description = "영화관 이름", example = "홍대점", requiredMode = Schema.RequiredMode.REQUIRED)
        String name
) implements TheaterManageCommand<Long> {
}
