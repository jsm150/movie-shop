package com.movie.shop.api.operator.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;

import com.movie.shop.api.operator.api.application.OperatorAuthenticationProvider;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class OperatorAuthenticationConfiguration {

    private final OperatorAuthenticationProvider operatorAuthenticationProvider;

    @Bean("operatorAuthenticationManager")
    public AuthenticationManager operatorAuthenticationManager() {
        return new ProviderManager(operatorAuthenticationProvider);
    }
}
