package com.movie.shop.api.operator.domain.condition;

import com.movie.shop.api.operator.domain.exceptions.OperatorDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OperatorTheaterPermissionScopeTargetTest {

    @Test
    @DisplayName("양수 영화관 식별자로 생성 성공한다")
    void create_withPositiveTheaterId_succeeds() {
        OperatorTheaterPermissionScopeTarget target = new OperatorTheaterPermissionScopeTarget(1L);

        assertThat(target.theaterId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("영화관 식별자가 0이면 생성 실패한다")
    void create_withZeroTheaterId_fails() {
        assertThatThrownBy(() -> new OperatorTheaterPermissionScopeTarget(0L))
                .isInstanceOf(OperatorDomainException.class)
                .hasMessageContaining("영화관 식별자는 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("영화관 식별자가 음수이면 생성 실패한다")
    void create_withNegativeTheaterId_fails() {
        assertThatThrownBy(() -> new OperatorTheaterPermissionScopeTarget(-1L))
                .isInstanceOf(OperatorDomainException.class)
                .hasMessageContaining("영화관 식별자는 0보다 커야 합니다.");
    }
}
