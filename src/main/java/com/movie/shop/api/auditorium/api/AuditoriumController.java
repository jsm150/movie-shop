package com.movie.shop.api.auditorium.api;

import an.awesome.pipelinr.Pipeline;
import com.movie.shop.api.auditorium.api.commands.ChangeStatusAuditoriumCommand;
import com.movie.shop.api.auditorium.api.commands.DeleteAuditoriumCommand;
import com.movie.shop.api.auditorium.api.commands.RegisterAuditoriumCommand;
import com.movie.shop.api.auditorium.api.commands.UpdateAuditoriumCommand;
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

@Tag(name = "상영관", description = "상영관 관리 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin/auditoriums")
@RequiredArgsConstructor
public class AuditoriumController {

    private final Pipeline pipeline;

    @Operation(summary = "상영관 등록", description = "새로운 상영관을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "상영관 등록 성공",
                    content = @Content(schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<Long> register(@RequestBody RegisterAuditoriumCommand command) {
        Long auditoriumId = pipeline.send(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(auditoriumId);
    }

    @Operation(summary = "상영관 수정", description = "상영관 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상영관 수정 성공",
                    content = @Content(schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content),
    })
    @PutMapping
    public ResponseEntity<Long> update(@RequestBody UpdateAuditoriumCommand command) {
        Long auditoriumId = pipeline.send(command);
        return ResponseEntity.ok().body(auditoriumId);
    }

    @Operation(summary = "상영관 상태 변경", description = "상영관 상태를 변경합니다. (활성화, 비활성화)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상영관 상태 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content),
    })
    @PatchMapping("/status")
    public ResponseEntity<Void> changeStatus(@RequestBody ChangeStatusAuditoriumCommand command) {
        pipeline.send(command);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "상영관 삭제", description = "상영관을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상영관 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content),
    })
    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestBody DeleteAuditoriumCommand command) {
        pipeline.send(command);
        return ResponseEntity.ok().build();
    }
}
