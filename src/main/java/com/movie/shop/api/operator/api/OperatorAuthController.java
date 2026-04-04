package com.movie.shop.api.operator.api;

import an.awesome.pipelinr.Pipeline;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.movie.shop.api.operator.api.commands.OperatorLoginCommand;
import com.movie.shop.api.operator.api.response.CurrentOperatorResponse;
import com.movie.shop.api.operator.api.response.OperatorLoginResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "관리자 인증", description = "관리자 인증 API")
@RestController
@RequestMapping("/operator/auth")
@RequiredArgsConstructor
public class OperatorAuthController {

    private final Pipeline pipeline;

    @Operation(summary = "로그인", description = "관리자 계정으로 로그인하고 JWT access token을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/login")
    public ResponseEntity<OperatorLoginResponse> login(@RequestBody OperatorLoginCommand command) {
        return ResponseEntity.ok(pipeline.send(command));
    }

    @Operation(summary = "현재 관리자 조회", description = "현재 로그인한 관리자 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "미인증")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<CurrentOperatorResponse> me(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(CurrentOperatorResponse.from(jwt));
    }
}
