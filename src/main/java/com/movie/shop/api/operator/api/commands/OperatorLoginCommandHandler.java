package com.movie.shop.api.operator.api.commands;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.movie.shop.api.operator.api.application.OperatorJwtTokenService;
import com.movie.shop.api.operator.api.response.OperatorLoginResponse;
import com.movie.shop.api.operator.domain.aggregate.Operator;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OperatorLoginCommandHandler implements Command.Handler<OperatorLoginCommand, OperatorLoginResponse> {

    @Qualifier("operatorAuthenticationManager")
    private final AuthenticationManager authenticationManager;
    private final OperatorJwtTokenService operatorJwtTokenService;

    @Override
    public OperatorLoginResponse handle(OperatorLoginCommand command) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(command.loginId(), command.password())
        );

        return operatorJwtTokenService.issue((Operator) authentication.getPrincipal());
    }
}
