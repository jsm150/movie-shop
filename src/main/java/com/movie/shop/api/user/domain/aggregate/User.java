package com.movie.shop.api.user.domain.aggregate;

import com.movie.shop.api.shared.domain.EntityValidator;
import com.movie.shop.api.user.domain.condition.OAuthIdentityUniquenessCondition;
import com.movie.shop.api.user.domain.exceptions.UserDomainException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "user_account")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long id;

    @NotNull(message = "OAuth 식별자는 필수입니다.")
    @Embedded
    private OAuthIdentity oauthIdentity;

    @Column(name = "email", length = 255)
    private Optional<UserEmail> email = Optional.empty();

    @NotNull(message = "사용자 이름은 필수입니다.")
    @AttributeOverride(
        name = "name",
        column = @Column(name = "name", nullable = false, length = 100)
    )
    @Embedded
    private UserName name;

    @NotNull(message = "사용자 상태는 필수입니다.")
    @Embedded
    private UserStatus status;

    public static User registerWithOAuth(
        OAuthIdentityUniquenessCondition oauthIdentityUniquenessCondition,
        OAuthProvider provider,
        String providerUserId,
        Optional<String> email,
        String name
    ) {
        var user = new User();
        user.status = new UserStatus.Active();
        user.oauthIdentity = OAuthIdentity.createNew(
            provider,
            providerUserId,
            oauthIdentityUniquenessCondition
        );
        user.email = createEmail(email);
        user.name = UserName.create(name);

        EntityValidator.create()
            .validateBean(user)
            .throwIfInvalid(UserDomainException::new);

        return user;
    }

    public void updateProfile(String name, Optional<String> email) {
        UserName newName = UserName.create(name);
        Optional<UserEmail> newEmail = createEmail(email);

        this.name = newName;
        this.email = newEmail;

        EntityValidator.create()
            .validateBean(this)
            .throwIfInvalid(UserDomainException::new);
    }

    public void suspend(
        UserSuspensionReason reason,
        long suspendedByOperatorId,
        OffsetDateTime suspendedAt
    ) {
        if (!(status instanceof UserStatus.Active)) {
            throw new UserDomainException("ACTIVE 상태의 사용자만 SUSPENDED 상태로 변경할 수 있습니다.");
        }

        status = new UserStatus.Suspended(reason, suspendedByOperatorId, suspendedAt);
        validate();
    }

    public void activate() {
        if (!(status instanceof UserStatus.Suspended)) {
            throw new UserDomainException("SUSPENDED 상태의 사용자만 ACTIVE 상태로 변경할 수 있습니다.");
        }

        status = new UserStatus.Active();
        validate();
    }

    public void withdraw() {
        if (status instanceof UserStatus.Withdrawn) {
            throw new UserDomainException("이미 탈퇴한 사용자는 다시 탈퇴 처리할 수 없습니다.");
        }

        status = new UserStatus.Withdrawn();
        validate();
    }

    private static Optional<UserEmail> createEmail(
        Optional<String> email
    ) {
        if (email == null) {
            throw new UserDomainException("이메일 Optional은 필수입니다.");
        }

        return email
            .map(value -> Optional.of(UserEmail.create(value)))
            .orElse(Optional.empty());
    }

    private void validate() {
        EntityValidator.create()
            .validateBean(this)
            .throwIfInvalid(UserDomainException::new);
    }
}
