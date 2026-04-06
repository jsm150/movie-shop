package com.movie.shop.api.theater.api.authorization;

import org.springframework.stereotype.Component;

import com.movie.shop.api.operator.domain.aggregate.Operator;
import com.movie.shop.api.operator.domain.aggregate.permission.OperatorAuthorizationRequirement;
import com.movie.shop.api.operator.domain.aggregate.permission.TheaterRequirementScope;
import com.movie.shop.api.operator.domain.port.LoadOperatorPort;
import com.movie.shop.api.shared.application.authorization.OperatorAuthorizationHandler;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TheaterManageAllAuthorizationHandler implements OperatorAuthorizationHandler<TheaterManageAllCommand<?>> {

    private final LoadOperatorPort loadOperatorPort;

    @Override
    public Class<?> supports() {
        return TheaterManageAllCommand.class;
    }

    @Override
    public void authorize(long operatorId, TheaterManageAllCommand<?> command) {
        Operator operator = loadOperatorPort.getById(operatorId);
        operator.authorize(new OperatorAuthorizationRequirement.RequireTheaterManage(
                new TheaterRequirementScope.AllTheaters()
        ));
    }
}
