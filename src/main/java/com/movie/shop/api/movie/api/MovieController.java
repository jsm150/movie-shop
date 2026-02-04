package com.movie.shop.api.movie.api;

import an.awesome.pipelinr.Pipeline;
import com.movie.shop.api.movie.api.commands.ChangeStateMovieCommand;
import com.movie.shop.api.movie.api.commands.DeleteMovieCommand;
import com.movie.shop.api.movie.api.commands.RegisterMovieCommand;
import com.movie.shop.api.movie.api.commands.UpdateMovieCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "영화", description = "영화 관리 API")
@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieController {

    private final Pipeline pipeline;

    @Operation(summary = "영화 등록", description = "새로운 영화를 등록합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "영화 등록 성공",
            content = @Content(schema = @Schema(implementation = Long.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content)
    })
    @PostMapping
    public ResponseEntity<Long> register(@RequestBody RegisterMovieCommand command) {
        Long movieId = pipeline.send(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(movieId);
    }

    @Operation(summary = "영화 수정", description = "영화 정보를 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "영화 수정 성공",
            content = @Content(schema = @Schema(implementation = Long.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content),
    })
    @PutMapping
    public ResponseEntity<Long> update(@RequestBody UpdateMovieCommand command) {
        Long movieId = pipeline.send(command);
        return ResponseEntity.ok().body(movieId);
    }

    @Operation(summary = "영화 삭제", description = "영화를 삭제합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "영화 삭제 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content),
    })
    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestBody DeleteMovieCommand command) {
        pipeline.send(command);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "영화 상태 변경", description = "영화의 상태를 변경합니다. (개봉 예정, 상영 중, 상영 종료)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "영화 상태 변경 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content),
    })
    @PatchMapping("/state")
    public ResponseEntity<Void> changeState(@RequestBody ChangeStateMovieCommand command) {
        pipeline.send(command);
        return ResponseEntity.ok().build();
    }
}
