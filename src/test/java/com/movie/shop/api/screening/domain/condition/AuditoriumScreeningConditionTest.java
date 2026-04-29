package com.movie.shop.api.screening.domain.condition;

import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuditoriumScreeningConditionTest {

    @Test
    @DisplayName("양수 영화관 ID로 생성 성공한다")
    void create_withPositiveTheaterId_succeeds() {
        AuditoriumScreeningCondition condition = new AuditoriumScreeningCondition(1L, true);

        assertThat(condition.theaterId()).isEqualTo(1L);
        assertThat(condition.canHostScreening()).isTrue();
    }

    @Test
    @DisplayName("영화관 ID가 0이면 생성 실패한다")
    void create_withZeroTheaterId_fails() {
        assertThatThrownBy(() -> new AuditoriumScreeningCondition(0L, true))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("영화관 ID는 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("영화관 ID가 음수이면 생성 실패한다")
    void create_withNegativeTheaterId_fails() {
        assertThatThrownBy(() -> new AuditoriumScreeningCondition(-1L, true))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("영화관 ID는 0보다 커야 합니다.");
    }
}
