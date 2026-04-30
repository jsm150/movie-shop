package com.movie.shop.api.user.domain.aggregate;

import com.movie.shop.api.user.domain.exceptions.UserDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserEmailTest {

    @Test
    @DisplayName("유효한 이메일을 생성한다")
    void create_withValidEmail_success() {
        UserEmail userEmail = UserEmail.create("user@example.com");

        assertThat(userEmail.getEmail()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("이메일은 필수다")
    void create_withNullEmail_fails() {
        assertThatThrownBy(() -> UserEmail.create(null))
                .isInstanceOf(UserDomainException.class)
                .hasMessageContaining("이메일은 필수입니다.");
    }

    @Test
    @DisplayName("이메일은 공백일 수 없다")
    void create_withBlankEmail_fails() {
        assertThatThrownBy(() -> UserEmail.create(" "))
                .isInstanceOf(UserDomainException.class)
                .hasMessageContaining("이메일은 필수입니다.");
    }

    @Test
    @DisplayName("이메일은 255자 이하여야 한다")
    void create_withTooLongEmail_fails() {
        String email = "a".repeat(244) + "@example.com";

        assertThatThrownBy(() -> UserEmail.create(email))
                .isInstanceOf(UserDomainException.class)
                .hasMessageContaining("이메일은 255자 이하여야 합니다.");
    }

    @Test
    @DisplayName("이메일 형식이 올바르지 않으면 생성할 수 없다")
    void create_withInvalidFormat_fails() {
        assertThatThrownBy(() -> UserEmail.create("invalid-email"))
                .isInstanceOf(UserDomainException.class)
                .hasMessageContaining("이메일 형식이 올바르지 않습니다.");
    }

    @Test
    @DisplayName("이메일의 여러 검증 오류를 함께 수집한다")
    void create_withMultipleInvalidValues_collectsErrors() {
        assertThatThrownBy(() -> UserEmail.create(" ".repeat(256)))
                .isInstanceOfSatisfying(UserDomainException.class, exception ->
                        assertThat(exception.getErrors()).contains(
                                "이메일은 필수입니다.",
                                "이메일은 255자 이하여야 합니다."
                        )
                );
    }
}
