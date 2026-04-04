package com.movie.shop.api.operator.api.application;

import java.time.Instant;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.stereotype.Service;

import com.movie.shop.api.application.configuration.JwtProperties;
import com.movie.shop.api.application.security.jwt.JwtIssuer;
import com.movie.shop.api.operator.api.response.CurrentOperatorResponse;
import com.movie.shop.api.operator.api.response.OperatorLoginResponse;
import com.movie.shop.api.operator.domain.aggregate.Operator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OperatorJwtTokenService {

    private final JwtIssuer jwtIssuer;
    private final JwtProperties jwtProperties;

    public OperatorLoginResponse issue(Operator operator) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(jwtProperties.getAccessTokenTtl());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(Long.toString(operator.getId()))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("loginId", operator.getLoginId())
                .claim("displayName", operator.getDisplayName())
                .claim("status", operator.getStatus().name())
                .build();

        return new OperatorLoginResponse(
                jwtIssuer.issue(claims),
                "Bearer",
                expiresAt,
                CurrentOperatorResponse.from(operator)
        );
    }
}
