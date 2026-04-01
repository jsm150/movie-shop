package com.movie.shop.api.operator.api.application;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.movie.shop.api.operator.domain.aggregate.Operator;
import com.movie.shop.api.operator.domain.aggregate.OperatorStatus;
import com.movie.shop.api.operator.domain.policy.PasswordPolicy;

public final class AuthenticatedOperatorPrincipal implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final long operatorId;
    private final String loginId;
    private final String displayName;
    private final OperatorStatus status;
    private final List<GrantedAuthority> authorities;

    private AuthenticatedOperatorPrincipal(long operatorId,
                                           String loginId,
                                           String displayName,
                                           OperatorStatus status,
                                           List<GrantedAuthority> authorities) {
        this.operatorId = operatorId;
        this.loginId = loginId;
        this.displayName = displayName;
        this.status = status;
        this.authorities = List.copyOf(authorities);
    }

    public static AuthenticatedOperatorPrincipal from(Operator operator, PasswordPolicy passwordPolicy, String rawPassword) {
        operator.authenticate(passwordPolicy, rawPassword);

        return new AuthenticatedOperatorPrincipal(
                operator.getId(),
                operator.getLoginId(),
                operator.getDisplayName(),
                operator.getStatus(),
                List.of(new SimpleGrantedAuthority("ROLE_OPERATOR"))
        );
    }

    public long getOperatorId() {
        return operatorId;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public OperatorStatus getStatus() {
        return status;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
}
