package com.movie.shop.api.user.domain.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.movie.shop.api.user.domain.exceptions.UserDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserSuspensionReasonTest {

    @Test
    @DisplayName("유효한 정지 사유를 생성한다")
    void create_withValidReason_success() {
        UserSuspensionReason reason = UserSuspensionReason.create(
            UserSuspensionReasonCode.POLICY_VIOLATION,
            "약관 위반"
        );

        assertThat(reason.getCode()).isEqualTo(UserSuspensionReasonCode.POLICY_VIOLATION);
        assertThat(reason.getMemo()).isEqualTo("약관 위반");
    }

    @Test
    @DisplayName("정지 사유 코드는 필수다")
    void create_withNullCode_fails() {
        assertThatThrownBy(() -> UserSuspensionReason.create(null, "약관 위반"))
            .isInstanceOf(UserDomainException.class)
            .hasMessageContaining("정지 사유 코드는 필수입니다.");
    }

    @Test
    @DisplayName("정지 사유 메모는 필수다")
    void create_withBlankMemo_fails() {
        assertThatThrownBy(() -> UserSuspensionReason.create(UserSuspensionReasonCode.OTHER, " "))
            .isInstanceOf(UserDomainException.class)
            .hasMessageContaining("정지 사유 메모는 필수입니다.");
    }

    @Test
    @DisplayName("정지 사유 메모는 500자 이하여야 한다")
    void create_withTooLongMemo_fails() {
        assertThatThrownBy(() -> UserSuspensionReason.create(UserSuspensionReasonCode.OTHER, "a".repeat(501)))
            .isInstanceOf(UserDomainException.class)
            .hasMessageContaining("정지 사유 메모는 500자 이하여야 합니다.");
    }
}
