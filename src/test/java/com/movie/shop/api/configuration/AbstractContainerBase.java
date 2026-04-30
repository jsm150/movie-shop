package com.movie.shop.api.configuration;

import java.time.Instant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.testcontainers.mysql.MySQLContainer;

import com.movie.shop.api.operator.domain.aggregate.Operator;
import com.movie.shop.api.operator.domain.aggregate.OperatorRepository;
import com.movie.shop.api.operator.domain.aggregate.permission.OperatorPermission;
import com.movie.shop.api.operator.domain.aggregate.permission.TheaterPermissionScope;

public class AbstractContainerBase {

    private static final String AUTHORIZATION_TEST_OPERATOR_LOGIN_ID = "__authorization_test_operator__";

    @ServiceConnection
    static final MySQLContainer MY_SQL_CONTAINER;

    @Autowired(required = false)
    private OperatorRepository operatorRepository;

    static {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        MY_SQL_CONTAINER = new MySQLContainer("mysql:8.4");
        MY_SQL_CONTAINER.start();
    }

    @BeforeEach
    void setUpAuthorizationTestOperator() {
        if (operatorRepository == null) {
            return;
        }

        Operator operator = loadOrCreateAuthorizationTestOperator();

        grantIfAbsent(operator, new OperatorPermission.MovieManagePermission());
        grantIfAbsent(operator, new OperatorPermission.TheaterManagePermission(new TheaterPermissionScope.AllTheaters()));
        grantIfAbsent(operator, new OperatorPermission.AuditoriumManagePermission(new TheaterPermissionScope.AllTheaters()));
        grantIfAbsent(operator, new OperatorPermission.ScreeningManagePermission(new TheaterPermissionScope.AllTheaters()));
        grantIfAbsent(operator, new OperatorPermission.OperatorManagePermission());

        operator = operatorRepository.save(operator);

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(
                Jwt.withTokenValue("authorization-test-token")
                        .header("alg", "none")
                        .subject(Long.toString(operator.getId()))
                        .issuedAt(Instant.now())
                        .expiresAt(Instant.now().plusSeconds(60))
                        .build(),
                java.util.List.of(new SimpleGrantedAuthority("ROLE_OPERATOR"))
        ));
    }

    @AfterEach
    void clearAuthorizationTestOperator() {
        SecurityContextHolder.clearContext();
    }

    private Operator loadOrCreateAuthorizationTestOperator() {
        if (operatorRepository.existsByLoginId(AUTHORIZATION_TEST_OPERATOR_LOGIN_ID)) {
            return operatorRepository.getByLoginId(AUTHORIZATION_TEST_OPERATOR_LOGIN_ID);
        }

        return Operator.register(
                AUTHORIZATION_TEST_OPERATOR_LOGIN_ID,
                "{noop}authorization-test-password",
                "Authorization Test Operator"
        );
    }

    private void grantIfAbsent(Operator operator, OperatorPermission permission) {
        if (!operator.getPermissions().contains(permission)) {
            operator.grant(permission);
        }
    }
}
