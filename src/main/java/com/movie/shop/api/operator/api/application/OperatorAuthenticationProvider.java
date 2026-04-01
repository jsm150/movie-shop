package com.movie.shop.api.operator.api.application;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.movie.shop.api.operator.domain.aggregate.Operator;
import com.movie.shop.api.operator.domain.aggregate.OperatorRepository;
import com.movie.shop.api.operator.domain.policy.PasswordPolicy;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OperatorAuthenticationProvider implements AuthenticationProvider {

    private final OperatorRepository operatorRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String loginId = authentication.getName();
        String rawPassword = authentication.getCredentials() == null
                ? ""
                : authentication.getCredentials().toString();

        Operator operator = operatorRepository.getByLoginId(loginId);

        AuthenticatedOperatorPrincipal principal = AuthenticatedOperatorPrincipal.from(
                operator, new PasswordPolicy(passwordEncoder), rawPassword);

        return UsernamePasswordAuthenticationToken.authenticated(
                principal,
                null,
                principal.getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
