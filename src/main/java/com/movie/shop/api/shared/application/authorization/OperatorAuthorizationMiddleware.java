package com.movie.shop.api.shared.application.authorization;

import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class OperatorAuthorizationMiddleware implements Command.Middleware {

    private final CurrentOperatorIdProvider currentOperatorIdProvider;
    private final List<OperatorAuthorizationHandler<?>> handlers;

    @Override
    public <R, C extends Command<R>> R invoke(C command, Next<R> next) {
        if (command instanceof OperatorAuthorizableCommand<?> authorizableCommand) {
            long operatorId = currentOperatorIdProvider.getCurrentOperatorId();
            int handledCount = 0;

            for (OperatorAuthorizationHandler<?> handler : handlers) {
                if (handler.supports().isInstance(authorizableCommand)) {
                    authorize(handler, operatorId, authorizableCommand);
                    handledCount++;
                }
            }

            if (handledCount == 0) {
                throw new IllegalStateException("운영자 인가 핸들러가 없습니다: " + command.getClass().getName());
            }
        }

        return next.invoke();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void authorize(
            OperatorAuthorizationHandler handler,
            long operatorId,
            OperatorAuthorizableCommand<?> command
    ) {
        handler.authorize(operatorId, command);
    }
}
