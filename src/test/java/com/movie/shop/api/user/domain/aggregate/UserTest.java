package com.movie.shop.api.user.domain.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.movie.shop.api.user.domain.condition.OAuthIdentityUniquenessCondition;
import com.movie.shop.api.user.domain.exceptions.UserDomainException;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTest {

    private static final OffsetDateTime SUSPENDED_AT =
        OffsetDateTime.parse("2026-04-30T10:00:00Z");

    @Test
    @DisplayName("Google OAuth 식별자와 이메일로 유저를 등록한다")
    void registerWithOAuth_withEmail_success() {
        User user = User.registerWithOAuth(
            uniqueOauthIdentity(),
            OAuthProvider.GOOGLE,
            "google-sub",
            Optional.of("user@example.com"),
            "User"
        );

        assertThat(user.getOauthIdentity().getProvider()).isEqualTo(OAuthProvider.GOOGLE);
        assertThat(user.getOauthIdentity().getProviderUserId()).isEqualTo("google-sub");
        assertThat(user.getEmail())
            .hasValueSatisfying(email -> assertThat(email.getEmail()).isEqualTo("user@example.com"));
        assertThat(user.getName().getName()).isEqualTo("User");
        assertThat(user.getStatus()).isEqualTo(new UserStatus.Active());
    }

    @Test
    @DisplayName("Optional.empty 이메일로 OAuth 식별자만 사용해 유저를 등록한다")
    void registerWithOAuth_withoutEmail_success() {
        User user = User.registerWithOAuth(
            uniqueOauthIdentity(),
            OAuthProvider.GOOGLE,
            "google-sub",
            Optional.empty(),
            "User"
        );

        assertThat(user.getEmail()).isEmpty();
    }

    @Test
    @DisplayName("OAuth 식별자 중복 조건은 필수다")
    void registerWithOAuth_withNullOauthIdentityUniquenessCondition_fails() {
        assertThatThrownBy(() -> User.registerWithOAuth(
            null,
            OAuthProvider.GOOGLE,
            "google-sub",
            Optional.empty(),
            "User"
        ))
            .isInstanceOf(UserDomainException.class)
            .hasMessageContaining("OAuth 식별자 중복 조건은 필수입니다.");
    }

    @Test
    @DisplayName("이미 존재하는 OAuth 식별자로 유저를 등록할 수 없다")
    void registerWithOAuth_withDuplicatedOauthIdentity_fails() {
        assertThatThrownBy(() -> User.registerWithOAuth(
            duplicatedOauthIdentity(),
            OAuthProvider.GOOGLE,
            "google-sub",
            Optional.empty(),
            "User"
        ))
            .isInstanceOf(UserDomainException.class)
            .hasMessageContaining("동일한 OAuth 식별자의 사용자가 이미 존재합니다.");
    }

    @Test
    @DisplayName("이메일 Optional은 필수다")
    void registerWithOAuth_withNullEmailOptional_fails() {
        assertThatThrownBy(() -> User.registerWithOAuth(
            uniqueOauthIdentity(),
            OAuthProvider.GOOGLE,
            "google-sub",
            null,
            "User"
        ))
            .isInstanceOf(UserDomainException.class)
            .hasMessageContaining("이메일 Optional은 필수입니다.");
    }

    @Test
    @DisplayName("OAuth 제공자는 필수다")
    void registerWithOAuth_withNullProvider_fails() {
        assertThatThrownBy(() -> User.registerWithOAuth(
            uniqueOauthIdentity(),
            null,
            "google-sub",
            Optional.of("user@example.com"),
            "User"
        ))
            .isInstanceOf(UserDomainException.class)
            .hasMessageContaining("OAuth 제공자는 필수입니다.");
    }

    @Test
    @DisplayName("OAuth 사용자 ID는 필수다")
    void registerWithOAuth_withBlankProviderUserId_fails() {
        assertThatThrownBy(() -> User.registerWithOAuth(
            uniqueOauthIdentity(),
            OAuthProvider.GOOGLE,
            " ",
            Optional.of("user@example.com"),
            "User"
        ))
            .isInstanceOf(UserDomainException.class)
            .hasMessageContaining("OAuth 사용자 ID는 필수입니다.");
    }

    @Test
    @DisplayName("OAuth 사용자 ID는 255자 이하여야 한다")
    void registerWithOAuth_withTooLongProviderUserId_fails() {
        assertThatThrownBy(() -> User.registerWithOAuth(
            uniqueOauthIdentity(),
            OAuthProvider.GOOGLE,
            "a".repeat(256),
            Optional.of("user@example.com"),
            "User"
        ))
            .isInstanceOf(UserDomainException.class)
            .hasMessageContaining("OAuth 사용자 ID는 255자 이하여야 합니다.");
    }

    @Test
    @DisplayName("제공된 이메일 형식이 올바르지 않으면 등록할 수 없다")
    void registerWithOAuth_withInvalidEmail_fails() {
        assertThatThrownBy(() -> User.registerWithOAuth(
            uniqueOauthIdentity(),
            OAuthProvider.GOOGLE,
            "google-sub",
            Optional.of("invalid-email"),
            "User"
        ))
            .isInstanceOf(UserDomainException.class)
            .hasMessageContaining("이메일 형식이 올바르지 않습니다.");
    }

    @Test
    @DisplayName("프로필 수정에서 이메일은 선택 값이다")
    void updateProfile_withOptionalEmail_success() {
        User user = registerUser();

        user.updateProfile("Changed", Optional.empty());

        assertThat(user.getName().getName()).isEqualTo("Changed");
        assertThat(user.getEmail()).isEmpty();
    }

    @Test
    @DisplayName("프로필 수정 실패 시 기존 프로필을 유지한다")
    void updateProfile_withInvalidEmail_keepsExistingProfile() {
        User user = registerUser();

        assertThatThrownBy(() -> user.updateProfile("Changed", Optional.of("invalid-email")))
            .isInstanceOf(UserDomainException.class)
            .hasMessageContaining("이메일 형식이 올바르지 않습니다.");

        assertThat(user.getName().getName()).isEqualTo("User");
        assertThat(user.getEmail())
            .hasValueSatisfying(email -> assertThat(email.getEmail()).isEqualTo("user@example.com"));
    }

    @Test
    @DisplayName("활성 유저를 정지하면 사유와 처리자와 처리 시각을 상태에 보존한다")
    void suspend_withActiveUser_success() {
        User user = registerUser();

        user.suspend(suspensionReason(), 7L, SUSPENDED_AT);

        assertThat(user.getStatus()).isInstanceOfSatisfying(
            UserStatus.Suspended.class,
            status -> {
                assertThat(status.getReason().getCode()).isEqualTo(UserSuspensionReasonCode.POLICY_VIOLATION);
                assertThat(status.getReason().getMemo()).isEqualTo("약관 위반");
                assertThat(status.getSuspendedByOperatorId()).isEqualTo(7L);
                assertThat(status.getSuspendedAt()).isEqualTo(SUSPENDED_AT);
            }
        );
    }

    @Test
    @DisplayName("정지 사유는 필수다")
    void suspend_withNullReason_fails() {
        User user = registerUser();

        assertThatThrownBy(() -> user.suspend(null, 1L, SUSPENDED_AT))
            .isInstanceOf(UserDomainException.class)
            .hasMessage("정지 사유는 필수입니다.");
    }

    @Test
    @DisplayName("정지 처리 운영자 ID는 0보다 커야 한다")
    void suspend_withInvalidOperatorId_fails() {
        User user = registerUser();

        assertThatThrownBy(() -> user.suspend(suspensionReason(), 0L, SUSPENDED_AT))
            .isInstanceOf(UserDomainException.class)
            .hasMessage("정지 처리 운영자 ID는 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("정지 처리 시각은 필수다")
    void suspend_withNullSuspendedAt_fails() {
        User user = registerUser();

        assertThatThrownBy(() -> user.suspend(suspensionReason(), 1L, null))
            .isInstanceOf(UserDomainException.class)
            .hasMessage("정지 처리 시각은 필수입니다.");
    }

    @Test
    @DisplayName("ACTIVE 상태가 아닌 유저는 정지할 수 없다")
    void suspend_withNonActiveUser_fails() {
        User user = registerUser();
        user.suspend(suspensionReason(), 1L, SUSPENDED_AT);

        assertThatThrownBy(() -> user.suspend(suspensionReason(), 1L, SUSPENDED_AT))
            .isInstanceOf(UserDomainException.class)
            .hasMessage("ACTIVE 상태의 사용자만 SUSPENDED 상태로 변경할 수 있습니다.");
    }

    @Test
    @DisplayName("정지 유저는 활성화할 수 있다")
    void activate_withSuspendedUser_success() {
        User user = registerUser();
        user.suspend(suspensionReason(), 1L, SUSPENDED_AT);

        user.activate();

        assertThat(user.getStatus()).isEqualTo(new UserStatus.Active());
    }

    @Test
    @DisplayName("SUSPENDED 상태가 아닌 유저는 활성화할 수 없다")
    void activate_withNonSuspendedUser_fails() {
        User user = registerUser();

        assertThatThrownBy(user::activate)
            .isInstanceOf(UserDomainException.class)
            .hasMessage("SUSPENDED 상태의 사용자만 ACTIVE 상태로 변경할 수 있습니다.");
    }

    @Test
    @DisplayName("활성 또는 정지 상태의 유저는 탈퇴할 수 있다")
    void withdraw_withActiveOrSuspendedUser_success() {
        User activeUser = registerUser("google-sub-1");
        User suspendedUser = registerUser("google-sub-2");
        suspendedUser.suspend(suspensionReason(), 1L, SUSPENDED_AT);

        activeUser.withdraw();
        suspendedUser.withdraw();

        assertThat(activeUser.getStatus()).isEqualTo(new UserStatus.Withdrawn());
        assertThat(suspendedUser.getStatus()).isEqualTo(new UserStatus.Withdrawn());
    }

    @Test
    @DisplayName("탈퇴한 유저는 다시 상태 전이를 할 수 없다")
    void transition_fromWithdrawn_fails() {
        User user = registerUser();
        user.withdraw();

        assertThatThrownBy(user::activate)
            .isInstanceOf(UserDomainException.class)
            .hasMessage("SUSPENDED 상태의 사용자만 ACTIVE 상태로 변경할 수 있습니다.");
        assertThatThrownBy(() -> user.suspend(suspensionReason(), 1L, SUSPENDED_AT))
            .isInstanceOf(UserDomainException.class)
            .hasMessage("ACTIVE 상태의 사용자만 SUSPENDED 상태로 변경할 수 있습니다.");
        assertThatThrownBy(user::withdraw)
            .isInstanceOf(UserDomainException.class)
            .hasMessage("이미 탈퇴한 사용자는 다시 탈퇴 처리할 수 없습니다.");
    }

    private User registerUser() {
        return registerUser("google-sub");
    }

    private User registerUser(String providerUserId) {
        return User.registerWithOAuth(
            uniqueOauthIdentity(),
            OAuthProvider.GOOGLE,
            providerUserId,
            Optional.of("user@example.com"),
            "User"
        );
    }

    private UserSuspensionReason suspensionReason() {
        return UserSuspensionReason.create(
            UserSuspensionReasonCode.POLICY_VIOLATION,
            "약관 위반"
        );
    }

    private OAuthIdentityUniquenessCondition uniqueOauthIdentity() {
        return new OAuthIdentityUniquenessCondition(true);
    }

    private OAuthIdentityUniquenessCondition duplicatedOauthIdentity() {
        return new OAuthIdentityUniquenessCondition(false);
    }
}
