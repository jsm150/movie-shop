package com.movie.shop.api.theater.api;

import an.awesome.pipelinr.Pipeline;
import com.movie.shop.api.theater.api.commands.RegisterTheaterCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "극장", description = "극장 관리 API")
@RestController
@RequestMapping("/theaters")
@RequiredArgsConstructor
public class TheaterController {

    private final Pipeline pipeline;

    @Operation(summary = "극장 등록", description = "새로운 극장을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "극장 등록 성공",
                    content = @Content(schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<Long> createTheater(@RequestBody RegisterTheaterCommand command) {
            Long theaterId = pipeline.send(command);
            return ResponseEntity.status(HttpStatus.CREATED).body(theaterId);
    }
}
