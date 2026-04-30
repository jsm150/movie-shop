package com.movie.shop.api.user.domain.aggregate;

import com.movie.shop.api.user.domain.exceptions.UserDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OAuthIdentityTest {

    @Test
    @DisplayName("유효한 OAuth 식별자를 생성한다")
    void create_withValidIdentity_success() {
        OAuthIdentity identity = OAuthIdentity.create(OAuthProvider.GOOGLE, "google-sub");

        assertThat(identity.getProvider()).isEqualTo(OAuthProvider.GOOGLE);
        assertThat(identity.getProviderUserId()).isEqualTo("google-sub");
    }

    @Test
    @DisplayName("OAuth 제공자는 필수다")
    void create_withNullProvider_fails() {
        assertThatThrownBy(() -> OAuthIdentity.create(null, "google-sub"))
                .isInstanceOf(UserDomainException.class)
                .hasMessageContaining("OAuth 제공자는 필수입니다.");
    }

    @Test
    @DisplayName("OAuth 사용자 ID는 필수다")
    void create_withNullProviderUserId_fails() {
        assertThatThrownBy(() -> OAuthIdentity.create(OAuthProvider.GOOGLE, null))
                .isInstanceOf(UserDomainException.class)
                .hasMessageContaining("OAuth 사용자 ID는 필수입니다.");
    }

    @Test
    @DisplayName("OAuth 사용자 ID는 공백일 수 없다")
    void create_withBlankProviderUserId_fails() {
        assertThatThrownBy(() -> OAuthIdentity.create(OAuthProvider.GOOGLE, " "))
                .isInstanceOf(UserDomainException.class)
                .hasMessageContaining("OAuth 사용자 ID는 필수입니다.");
    }

    @Test
    @DisplayName("OAuth 사용자 ID는 255자 이하여야 한다")
    void create_withTooLongProviderUserId_fails() {
        assertThatThrownBy(() -> OAuthIdentity.create(OAuthProvider.GOOGLE, "a".repeat(256)))
                .isInstanceOf(UserDomainException.class)
                .hasMessageContaining("OAuth 사용자 ID는 255자 이하여야 합니다.");
    }

    @Test
    @DisplayName("OAuth 식별자의 여러 검증 오류를 함께 수집한다")
    void create_withMultipleInvalidValues_collectsErrors() {
        assertThatThrownBy(() -> OAuthIdentity.create(null, " ".repeat(256)))
                .isInstanceOfSatisfying(UserDomainException.class, exception ->
                        assertThat(exception.getErrors()).contains(
                                "OAuth 제공자는 필수입니다.",
                                "OAuth 사용자 ID는 필수입니다.",
                                "OAuth 사용자 ID는 255자 이하여야 합니다."
                        )
                );
    }
}
