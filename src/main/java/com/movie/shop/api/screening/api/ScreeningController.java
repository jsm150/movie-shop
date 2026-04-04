package com.movie.shop.api.screening.api;

import an.awesome.pipelinr.Pipeline;
import com.movie.shop.api.screening.api.commands.ChangeStateScreeningCommand;
import com.movie.shop.api.screening.api.commands.DeleteScreeningCommand;
import com.movie.shop.api.screening.api.commands.RegisterScreeningCommand;
import com.movie.shop.api.screening.api.commands.UpdateScreeningCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "상영", description = "상영 관리 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/screenings")
@RequiredArgsConstructor
public class ScreeningController {

    private final Pipeline pipeline;

    @Operation(summary = "상영 등록", description = "새로운 상영 일정을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "상영 등록 성공",
                    content = @Content(schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<Long> createScreening(@RequestBody RegisterScreeningCommand command) {
        Long screeningId = pipeline.send(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(screeningId);
    }

    @Operation(summary = "상영 수정", description = "상영 일정을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상영 수정 성공",
                    content = @Content(schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content),
    })
    @PutMapping
    public ResponseEntity<Long> updateScreening(@RequestBody UpdateScreeningCommand command) {
        Long screeningId = pipeline.send(command);
        return ResponseEntity.ok().body(screeningId);
    }

    @Operation(summary = "상영 상태 변경", description = "상영 상태를 변경합니다. (판매 오픈, 판매 종료, 취소, 상영 종료)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상영 상태 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content),
    })
    @PatchMapping("/state")
    public ResponseEntity<Void> changeState(@RequestBody ChangeStateScreeningCommand command) {
        pipeline.send(command);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "상영 삭제", description = "상영을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상영 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content),
    })
    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestBody DeleteScreeningCommand command) {
        pipeline.send(command);
        return ResponseEntity.ok().build();
    }
}
