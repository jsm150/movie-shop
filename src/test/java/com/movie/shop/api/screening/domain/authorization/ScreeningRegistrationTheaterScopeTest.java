package com.movie.shop.api.screening.domain.authorization;

import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScreeningRegistrationTheaterScopeTest {

    @Test
    @DisplayName("양수 영화관 ID로 생성 성공한다")
    void create_withPositiveTheaterId_succeeds() {
        ScreeningRegistrationTheaterScope scope = new ScreeningRegistrationTheaterScope(1L);

        assertThat(scope.theaterId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("영화관 ID가 0이면 생성 실패한다")
    void create_withZeroTheaterId_fails() {
        assertThatThrownBy(() -> new ScreeningRegistrationTheaterScope(0L))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("영화관 ID는 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("영화관 ID가 음수이면 생성 실패한다")
    void create_withNegativeTheaterId_fails() {
        assertThatThrownBy(() -> new ScreeningRegistrationTheaterScope(-1L))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("영화관 ID는 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("상영관 정보가 없으면 요구 범위 조회에 실패한다")
    void require_withEmptyTheaterScope_fails() {
        assertThatThrownBy(() -> ScreeningRegistrationTheaterScope.require(Optional.empty()))
                .isInstanceOf(ScreeningDomainException.class)
                .hasMessageContaining("상영관 정보를 찾을 수 없습니다.");
    }
}
