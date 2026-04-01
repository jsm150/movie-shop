package com.movie.shop.api.operator.api;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.movie.shop.api.operator.api.application.AuthenticatedOperatorPrincipal;
import com.movie.shop.api.operator.api.dto.CurrentOperatorResponse;
import com.movie.shop.api.operator.api.dto.OperatorLoginRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Tag(name = "관리자 인증", description = "관리자 인증 API")
@RestController
@RequestMapping("/operator/auth")
@RequiredArgsConstructor
public class OperatorAuthController {

    @Qualifier("operatorAuthenticationManager")
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    @Operation(summary = "로그인", description = "운영자 계정으로 로그인하고 세션을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/login")
    public ResponseEntity<CurrentOperatorResponse> login(@RequestBody OperatorLoginRequest request,
                                                         HttpServletRequest httpServletRequest,
                                                         HttpServletResponse httpServletResponse) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.loginId(), request.password())
        );

        SecurityContext securityContext = new SecurityContextImpl(authentication);
        SecurityContextHolder.setContext(securityContext);
        securityContextRepository.saveContext(securityContext, httpServletRequest, httpServletResponse);

        return ResponseEntity.ok(CurrentOperatorResponse.from((AuthenticatedOperatorPrincipal) authentication.getPrincipal()));
    }

    @Operation(summary = "로그아웃", description = "현재 세션을 무효화합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "로그아웃 성공")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request,
                                       HttpServletResponse response,
                                       Authentication authentication) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "현재 운영자 조회", description = "현재 로그인한 운영자 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "미인증")
    })
    @GetMapping("/me")
    public ResponseEntity<CurrentOperatorResponse> me(
            @AuthenticationPrincipal AuthenticatedOperatorPrincipal principal
    ) {
        return ResponseEntity.ok(CurrentOperatorResponse.from(principal));
    }
}
