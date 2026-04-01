package com.movie.shop.api.operator.domain.aggregate;

import org.springframework.security.authentication.BadCredentialsException;

import com.movie.shop.api.operator.domain.exceptions.OperatorDomainException;
import com.movie.shop.api.operator.domain.policy.PasswordPolicy;
import com.movie.shop.api.shared.domain.EntityValidator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "operator_account")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Operator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "operator_id", nullable = false)
    private Long id;

    @NotBlank(message = "로그인 ID는 필수입니다.")
    @Size(max = 50, message = "로그인 ID는 50자 이하여야 합니다.")
    @Column(name = "login_id", nullable = false, length = 50, unique = true)
    private String loginId;

    @NotBlank(message = "비밀번호 해시는 필수입니다.")
    @Size(max = 100, message = "비밀번호 해시는 100자 이하여야 합니다.")
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @NotBlank(message = "표시 이름은 필수입니다.")
    @Size(max = 100, message = "표시 이름은 100자 이하여야 합니다.")
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @NotNull(message = "운영자 상태는 필수입니다.")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OperatorStatus status;

    public static Operator register(String loginId, String passwordHash, String displayName) {
        var operator = new Operator();
        operator.loginId = loginId;
        operator.passwordHash = passwordHash;
        operator.displayName = displayName;
        operator.status = OperatorStatus.ACTIVE;

        EntityValidator.create()
                .validateBean(operator)
                .throwIfInvalid(OperatorDomainException::new);

        return operator;
    }

    public void suspend() {
        status = OperatorStatus.SUSPENDED;
    }

    public void activate() {
        status = OperatorStatus.ACTIVE;
    }

    public void authenticate(PasswordPolicy passwordPolicy, String rawPassword) {
        if (status != OperatorStatus.ACTIVE) {
            throw new BadCredentialsException("비활성화된 계정입니다.");
        }
        passwordPolicy.validate(rawPassword, passwordHash);
    }
}
