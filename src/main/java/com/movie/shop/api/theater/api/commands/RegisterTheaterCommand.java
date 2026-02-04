package com.movie.shop.api.theater.api.commands;

import an.awesome.pipelinr.Command;
import com.movie.shop.api.theater.domain.aggregate.TheaterType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "극장 등록 요청")
public record RegisterTheaterCommand(
        @Schema(description = "상영관 이름", example = "1관", requiredMode = Schema.RequiredMode.REQUIRED)
        String name,

        @Schema(description = "층수", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        int floor,

        @Schema(description = "상영관 타입", example = "Standard", requiredMode = Schema.RequiredMode.REQUIRED)
        TheaterType type,

        @Schema(description = "좌석 코드 목록", example = "[\"A1\", \"A2\", \"A3\"]", requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> seats,

        @Schema(description = "행 수", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        int rowCount,

        @Schema(description = "열 수", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
        int columnCount
) implements Command<Long> { }
