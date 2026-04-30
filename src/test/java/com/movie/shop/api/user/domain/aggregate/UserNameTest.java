package com.movie.shop.api.user.domain.aggregate;

import com.movie.shop.api.user.domain.exceptions.UserDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserNameTest {

    @Test
    @DisplayName("유효한 사용자 이름을 생성한다")
    void create_withValidName_success() {
        UserName userName = UserName.create("User");

        assertThat(userName.getName()).isEqualTo("User");
    }

    @Test
    @DisplayName("사용자 이름은 필수다")
    void create_withNullName_fails() {
        assertThatThrownBy(() -> UserName.create(null))
                .isInstanceOf(UserDomainException.class)
                .hasMessageContaining("사용자 이름은 필수입니다.");
    }

    @Test
    @DisplayName("사용자 이름은 공백일 수 없다")
    void create_withBlankName_fails() {
        assertThatThrownBy(() -> UserName.create(" "))
                .isInstanceOf(UserDomainException.class)
                .hasMessageContaining("사용자 이름은 필수입니다.");
    }

    @Test
    @DisplayName("사용자 이름은 100자 이하여야 한다")
    void create_withTooLongName_fails() {
        assertThatThrownBy(() -> UserName.create("a".repeat(101)))
                .isInstanceOf(UserDomainException.class)
                .hasMessageContaining("사용자 이름은 100자 이하여야 합니다.");
    }

    @Test
    @DisplayName("사용자 이름의 여러 검증 오류를 함께 수집한다")
    void create_withMultipleInvalidValues_collectsErrors() {
        assertThatThrownBy(() -> UserName.create(" ".repeat(101)))
                .isInstanceOfSatisfying(UserDomainException.class, exception ->
                        assertThat(exception.getErrors()).contains(
                                "사용자 이름은 필수입니다.",
                                "사용자 이름은 100자 이하여야 합니다."
                        )
                );
    }
}
