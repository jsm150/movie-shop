package com.movie.shop.api.screening.api.authorization;

import org.springframework.stereotype.Component;

import com.movie.shop.api.operator.domain.aggregate.Operator;
import com.movie.shop.api.operator.domain.aggregate.permission.OperatorAuthorizationRequirement;
import com.movie.shop.api.operator.domain.aggregate.permission.TheaterRequirementScope;
import com.movie.shop.api.operator.domain.port.LoadOperatorPort;
import com.movie.shop.api.screening.api.commands.ChangeStateScreeningCommand;
import com.movie.shop.api.screening.api.commands.DeleteScreeningCommand;
import com.movie.shop.api.screening.api.commands.RegisterScreeningCommand;
import com.movie.shop.api.screening.api.commands.UpdateScreeningCommand;
import com.movie.shop.api.screening.domain.authorization.ScreeningRegistrationTheaterScope;
import com.movie.shop.api.screening.domain.aggregate.ScreeningRepository;
import com.movie.shop.api.screening.domain.port.ScreeningRegistrationTheaterScopePort;
import com.movie.shop.api.shared.application.authorization.OperatorAuthorizationHandler;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ScreeningManageAuthorizationHandler implements OperatorAuthorizationHandler<ScreeningManageCommand<?>> {

    private final LoadOperatorPort loadOperatorPort;
    private final ScreeningRegistrationTheaterScopePort screeningRegistrationTheaterScopePort;
    private final ScreeningRepository screeningRepository;

    @Override
    public Class<?> supports() {
        return ScreeningManageCommand.class;
    }

    @Override
    public void authorize(long operatorId, ScreeningManageCommand<?> command) {
        Operator operator = loadOperatorPort.getById(operatorId);
        operator.authorize(new OperatorAuthorizationRequirement.RequireScreeningManage(
                new TheaterRequirementScope.SingleTheater(resolveTheaterId(command))
        ));
    }

    private long resolveTheaterId(ScreeningManageCommand<?> command) {
        if (command instanceof RegisterScreeningCommand registerCommand) {
            return ScreeningRegistrationTheaterScope.require(
                    screeningRegistrationTheaterScopePort.findTheaterScope(registerCommand.auditoriumId())
            ).theaterId();
        }

        if (command instanceof UpdateScreeningCommand updateCommand) {
            return screeningRepository.getById(updateCommand.screeningId()).getTheaterId();
        }

        if (command instanceof DeleteScreeningCommand deleteCommand) {
            return screeningRepository.getById(deleteCommand.screeningId()).getTheaterId();
        }

        if (command instanceof ChangeStateScreeningCommand changeStateCommand) {
            return screeningRepository.getById(changeStateCommand.screeningId()).getTheaterId();
        }

        throw new IllegalStateException("지원하지 않는 상영 인가 요청입니다: " + command.getClass().getName());
    }
}
