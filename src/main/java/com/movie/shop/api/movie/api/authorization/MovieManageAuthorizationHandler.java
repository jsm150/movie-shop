package com.movie.shop.api.movie.api.authorization;

import org.springframework.stereotype.Component;

import com.movie.shop.api.operator.domain.aggregate.Operator;
import com.movie.shop.api.operator.domain.aggregate.permission.OperatorAuthorizationRequirement;
import com.movie.shop.api.operator.domain.port.LoadOperatorPort;
import com.movie.shop.api.shared.application.authorization.OperatorAuthorizationHandler;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MovieManageAuthorizationHandler implements OperatorAuthorizationHandler<MovieManageCommand<?>> {

    private final LoadOperatorPort loadOperatorPort;

    @Override
    public Class<?> supports() {
        return MovieManageCommand.class;
    }

    @Override
    public void authorize(long operatorId, MovieManageCommand<?> command) {
        Operator operator = loadOperatorPort.getById(operatorId);
        operator.authorize(new OperatorAuthorizationRequirement.RequireMovieManage());
    }
}
