package com.movie.shop.api.auditorium.domain.condition;

import com.movie.shop.api.auditorium.domain.exceptions.AuditoriumDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuditoriumRegistrationTheaterTest {

    @Test
    @DisplayName("양수 영화관 ID로 생성 성공한다")
    void create_withPositiveTheaterId_succeeds() {
        AuditoriumRegistrationTheater condition = new AuditoriumRegistrationTheater(1L);

        assertThat(condition.theaterId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("영화관 ID가 0이면 생성 실패한다")
    void create_withZeroTheaterId_fails() {
        assertThatThrownBy(() -> new AuditoriumRegistrationTheater(0L))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("영화관 ID는 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("영화관 ID가 음수이면 생성 실패한다")
    void create_withNegativeTheaterId_fails() {
        assertThatThrownBy(() -> new AuditoriumRegistrationTheater(-1L))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("영화관 ID는 0보다 커야 합니다.");
    }
}
