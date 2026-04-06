package com.movie.shop.api.shared.application.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import an.awesome.pipelinr.Command;

class OperatorAuthorizationMiddlewareTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("인가 대상 요청은 매칭되는 핸들러를 실행한 뒤 다음 단계로 넘긴다")
    void invoke_withOperatorAuthorizableCommand_runsMatchingHandler() {
        authenticateAs(1L);
        RecordingHandler handler = new RecordingHandler(MovieManageCommand.class);
        OperatorAuthorizationMiddleware middleware = middleware(List.of(handler));
        AtomicBoolean invoked = new AtomicBoolean(false);

        String result = middleware.invoke(
                new AuthorizedRequest(),
                () -> {
                    invoked.set(true);
                    return "next";
                }
        );

        assertThat(handler.invocationCount()).isEqualTo(1);
        assertThat(handler.lastOperatorId()).isEqualTo(1L);
        assertThat(invoked.get()).isTrue();
        assertThat(result).isEqualTo("next");
    }

    @Test
    @DisplayName("여러 인가 타입을 구현한 요청은 매칭되는 핸들러를 모두 실행한다")
    void invoke_withMultipleAuthorizationTypes_runsAllMatchingHandlers() {
        authenticateAs(2L);
        RecordingHandler movieHandler = new RecordingHandler(MovieManageCommand.class);
        RecordingHandler theaterHandler = new RecordingHandler(TheaterManageCommand.class);
        OperatorAuthorizationMiddleware middleware = middleware(List.of(movieHandler, theaterHandler));

        middleware.invoke(new MultiAuthorizedRequest(), () -> "next");

        assertThat(movieHandler.invocationCount()).isEqualTo(1);
        assertThat(theaterHandler.invocationCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("인가 대상 요청에 매칭되는 핸들러가 없으면 실패한다")
    void invoke_withoutMatchingHandler_fails() {
        authenticateAs(1L);
        OperatorAuthorizationMiddleware middleware = middleware(List.of());

        assertThatThrownBy(() -> middleware.invoke(new AuthorizedRequest(), () -> "next"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("운영자 인가 핸들러가 없습니다");
    }

    @Test
    @DisplayName("운영자 식별자가 숫자가 아니면 인증 예외로 실패한다")
    void invoke_withInvalidSubject_failsWithAuthenticationException() {
        authenticateAs("not-number");
        OperatorAuthorizationMiddleware middleware = middleware(List.of(new RecordingHandler(MovieManageCommand.class)));

        assertThatThrownBy(() -> middleware.invoke(new AuthorizedRequest(), () -> "next"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("운영자 식별자가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("비대상 요청은 인증 없이도 그대로 다음 단계로 넘긴다")
    void invoke_withoutOperatorAuthorizableCommand_passesThrough() {
        OperatorAuthorizationMiddleware middleware = middleware(List.of());
        AtomicBoolean invoked = new AtomicBoolean(false);

        String result = middleware.invoke(
                new PlainRequest(),
                () -> {
                    invoked.set(true);
                    return "next";
                }
        );

        assertThat(invoked.get()).isTrue();
        assertThat(result).isEqualTo("next");
    }

    private OperatorAuthorizationMiddleware middleware(List<OperatorAuthorizationHandler<?>> handlers) {
        return new OperatorAuthorizationMiddleware(new CurrentOperatorIdProvider(), handlers);
    }

    private void authenticateAs(long operatorId) {
        authenticateAs(Long.toString(operatorId));
    }

    private void authenticateAs(String subject) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(subject)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(
                jwt,
                List.of(new SimpleGrantedAuthority("ROLE_OPERATOR"))
        ));
    }

    record AuthorizedRequest() implements MovieManageCommand<String> {
    }

    record MultiAuthorizedRequest() implements MovieManageCommand<String>, TheaterManageCommand<String> {
    }

    interface MovieManageCommand<R> extends OperatorAuthorizableCommand<R> {
    }

    interface TheaterManageCommand<R> extends OperatorAuthorizableCommand<R> {
    }

    record PlainRequest() implements Command<String> {
    }

    private static class RecordingHandler implements OperatorAuthorizationHandler<OperatorAuthorizableCommand<?>> {

        private final Class<?> supportedType;
        private final AtomicInteger invocationCount = new AtomicInteger();
        private long lastOperatorId;

        private RecordingHandler(Class<?> supportedType) {
            this.supportedType = supportedType;
        }

        @Override
        public Class<?> supports() {
            return supportedType;
        }

        @Override
        public void authorize(long operatorId, OperatorAuthorizableCommand<?> command) {
            invocationCount.incrementAndGet();
            lastOperatorId = operatorId;
        }

        private int invocationCount() {
            return invocationCount.get();
        }

        private long lastOperatorId() {
            return lastOperatorId;
        }
    }
}
