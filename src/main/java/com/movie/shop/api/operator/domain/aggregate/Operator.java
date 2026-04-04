package com.movie.shop.api.operator.domain.aggregate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.security.authentication.BadCredentialsException;

import com.movie.shop.api.operator.domain.aggregate.permission.OperatorAuthorizationRequirement;
import com.movie.shop.api.operator.domain.aggregate.permission.OperatorPermission;
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

    @JdbcTypeCode(SqlTypes.JSON)
    @NotNull(message = "권한 목록은 필수입니다.")
    @Column(name = "permissions_json", columnDefinition = "json", nullable = false)
    private List<OperatorPermission> permissions = new ArrayList<>();

    public static Operator register(String loginId, String passwordHash, String displayName) {
        var operator = new Operator();
        operator.loginId = loginId;
        operator.passwordHash = passwordHash;
        operator.displayName = displayName;
        operator.status = OperatorStatus.ACTIVE;
        operator.permissions = new ArrayList<>();

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

    public void grant(OperatorPermission permission) {
        if (permission == null) {
            throw new OperatorDomainException("부여할 권한은 필수입니다.");
        }

        if (permissions.contains(permission)) {
            throw new OperatorDomainException("이미 부여된 권한입니다.");
        }

        permissions.add(permission);
    }

    public void revoke(OperatorPermission permission) {
        if (permission == null) {
            throw new OperatorDomainException("회수할 권한은 필수입니다.");
        }

        if (!permissions.remove(permission)) {
            throw new OperatorDomainException("부여되지 않은 권한은 회수할 수 없습니다.");
        }
    }

    public void authorize(OperatorAuthorizationRequirement requirement) {
        if (status != OperatorStatus.ACTIVE) {
            throw new OperatorDomainException("비활성화된 운영자는 권한을 행사할 수 없습니다.");
        }

        if (!hasPermission(requirement)) {
            throw new OperatorDomainException("운영자 권한이 없습니다.");
        }
    }

    public boolean hasPermission(OperatorAuthorizationRequirement requirement) {
        if (requirement == null) {
            throw new OperatorDomainException("인가 요구사항은 필수입니다.");
        }

        return requirement.isSatisfiedBy(permissions);
    }
}
