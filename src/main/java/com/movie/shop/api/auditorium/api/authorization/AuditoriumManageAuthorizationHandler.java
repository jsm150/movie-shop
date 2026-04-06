package com.movie.shop.api.auditorium.api.authorization;

import org.springframework.stereotype.Component;

import com.movie.shop.api.auditorium.api.commands.ChangeStatusAuditoriumCommand;
import com.movie.shop.api.auditorium.api.commands.DeleteAuditoriumCommand;
import com.movie.shop.api.auditorium.api.commands.RegisterAuditoriumCommand;
import com.movie.shop.api.auditorium.api.commands.UpdateAuditoriumCommand;
import com.movie.shop.api.auditorium.domain.aggregate.AuditoriumRepository;
import com.movie.shop.api.operator.domain.aggregate.Operator;
import com.movie.shop.api.operator.domain.aggregate.permission.OperatorAuthorizationRequirement;
import com.movie.shop.api.operator.domain.aggregate.permission.TheaterRequirementScope;
import com.movie.shop.api.operator.domain.port.LoadOperatorPort;
import com.movie.shop.api.shared.application.authorization.OperatorAuthorizationHandler;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuditoriumManageAuthorizationHandler implements OperatorAuthorizationHandler<AuditoriumManageCommand<?>> {

    private final LoadOperatorPort loadOperatorPort;
    private final AuditoriumRepository auditoriumRepository;

    @Override
    public Class<?> supports() {
        return AuditoriumManageCommand.class;
    }

    @Override
    public void authorize(long operatorId, AuditoriumManageCommand<?> command) {
        Operator operator = loadOperatorPort.getById(operatorId);
        operator.authorize(new OperatorAuthorizationRequirement.RequireAuditoriumManage(
                new TheaterRequirementScope.SingleTheater(resolveTheaterId(command))
        ));
    }

    private long resolveTheaterId(AuditoriumManageCommand<?> command) {
        if (command instanceof RegisterAuditoriumCommand registerCommand) {
            return registerCommand.theaterId();
        }

        if (command instanceof UpdateAuditoriumCommand updateCommand) {
            return auditoriumRepository.getById(updateCommand.auditoriumId()).getTheaterId();
        }

        if (command instanceof DeleteAuditoriumCommand deleteCommand) {
            return auditoriumRepository.getById(deleteCommand.auditoriumId()).getTheaterId();
        }

        if (command instanceof ChangeStatusAuditoriumCommand changeStatusCommand) {
            return auditoriumRepository.getById(changeStatusCommand.auditoriumId()).getTheaterId();
        }

        throw new IllegalStateException("지원하지 않는 상영관 인가 요청입니다: " + command.getClass().getName());
    }
}
