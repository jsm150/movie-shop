package com.movie.shop.api.screening.domain.condition;

import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MovieSchedulingConditionTest {

    @Test
    @DisplayName("양수 영화 런타임으로 생성 성공한다")
    void create_withPositiveRuntimeMinutes_succeeds() {
        MovieSchedulingCondition condition = new MovieSchedulingCondition(true, 120);

        assertThat(condition.canBeScheduled()).isTrue();
        assertThat(condition.runtimeMinutes()).isEqualTo(120);
    }

    @Test
    @DisplayName("영화 런타임이 0이면 생성 실패한다")
    void create_withZeroRuntimeMinutes_fails() {
        assertThatThrownBy(() -> new MovieSchedulingCondition(true, 0))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("영화 런타임은 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("영화 런타임이 음수이면 생성 실패한다")
    void create_withNegativeRuntimeMinutes_fails() {
        assertThatThrownBy(() -> new MovieSchedulingCondition(true, -1))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("영화 런타임은 0보다 커야 합니다.");
    }
}
