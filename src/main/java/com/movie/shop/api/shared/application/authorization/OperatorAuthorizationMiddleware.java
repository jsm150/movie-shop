package com.movie.shop.api.shared.application.authorization;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import an.awesome.pipelinr.Command;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OperatorAuthorizationMiddleware implements Command.Middleware {

    @Override
    public <R, C extends Command<R>> R invoke(C command, Next<R> next) {
        if (command instanceof OperatorAuthorizableCommand<?>) {
            // Authorization handlers will be wired in the next step.
        }

        return next.invoke();
    }
}
