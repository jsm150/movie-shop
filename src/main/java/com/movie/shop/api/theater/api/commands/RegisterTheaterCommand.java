package com.movie.shop.api.theater.api.commands;

import an.awesome.pipelinr.Command;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "영화관 등록 요청")
public record RegisterTheaterCommand(
        @Schema(description = "영화관 이름", example = "강남점", requiredMode = Schema.RequiredMode.REQUIRED)
        String name
) implements Command<Long> { }
