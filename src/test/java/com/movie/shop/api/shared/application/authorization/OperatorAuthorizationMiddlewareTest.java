package com.movie.shop.api.shared.application.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import an.awesome.pipelinr.Command;

class OperatorAuthorizationMiddlewareTest {

    private final OperatorAuthorizationMiddleware middleware = new OperatorAuthorizationMiddleware();

    @Test
    @DisplayName("인가 대상 요청은 현재 단계에서 그대로 다음 단계로 넘긴다")
    void invoke_withOperatorAuthorizableCommand_passesThrough() {
        AtomicBoolean invoked = new AtomicBoolean(false);

        String result = middleware.invoke(
                new AuthorizedRequest(),
                () -> {
                    invoked.set(true);
                    return "next";
                }
        );

        assertThat(invoked.get()).isTrue();
        assertThat(result).isEqualTo("next");
    }

    @Test
    @DisplayName("비대상 요청도 그대로 다음 단계로 넘긴다")
    void invoke_withoutOperatorAuthorizableCommand_passesThrough() {
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

    record AuthorizedRequest() implements MovieManageCommand<String> {
    }

    interface MovieManageCommand<R> extends OperatorAuthorizableCommand<R> {
    }

    record PlainRequest() implements Command<String> {
    }
}
