package com.movie.shop.api.auditorium.domain.aggregate;

import com.movie.shop.api.auditorium.domain.exceptions.AuditoriumDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuditoriumSeatsTest {

    @Test
    @DisplayName("좌석 생성 성공한다")
    void createSuccess() {
        List<String> seats = List.of("A1", "A2", "A3", "B1", "B2", "B3");

        AuditoriumSeats auditoriumSeats = AuditoriumSeats.create(seats, 2, 3);

        assertThat(auditoriumSeats.getSeats()).isEqualTo(seats);
        assertThat(auditoriumSeats.getRowCount()).isEqualTo(2);
        assertThat(auditoriumSeats.getColumnCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("좌석 리스트가 비어 있으면 실패한다")
    void createFailWhenSeatsEmpty() {
        assertThatThrownBy(() -> AuditoriumSeats.create(List.of(), 2, 3))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("최소 하나 이상의 좌석이 필요합니다.");
    }

    @Test
    @DisplayName("좌석 수가 행*열과 다르면 실패한다")
    void createFailWhenSeatCountMismatch() {
        List<String> seats = List.of("A1", "A2", "A3");

        assertThatThrownBy(() -> AuditoriumSeats.create(seats, 2, 2))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("좌석 수가 행과 열의 곱과 일치하지 않습니다.");
    }

    @Test
    @DisplayName("중복된 좌석이 있으면 실패한다")
    void createFailWhenSeatDuplicated() {
        List<String> seats = List.of("A1", "A1", "A2", "A3");

        assertThatThrownBy(() -> AuditoriumSeats.create(seats, 2, 2))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("중복된 좌석이 있습니다.");
    }

    @Test
    @DisplayName("행 수가 0 이하면 실패한다")
    void createFailWhenRowCountTooSmall() {
        List<String> seats = List.of("A1", "A2");

        assertThatThrownBy(() -> AuditoriumSeats.create(seats, 0, 2))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("행 수는 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("행 수가 100 초과면 실패한다")
    void createFailWhenRowCountTooLarge() {
        List<String> seats = List.of("A1", "A2");

        assertThatThrownBy(() -> AuditoriumSeats.create(seats, 101, 1))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("행 수는 100을 초과할 수 없습니다.");
    }

    @Test
    @DisplayName("열 수가 0 이하면 실패한다")
    void createFailWhenColumnCountTooSmall() {
        List<String> seats = List.of("A1", "A2");

        assertThatThrownBy(() -> AuditoriumSeats.create(seats, 1, 0))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("열 수는 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("열 수가 50 초과면 실패한다")
    void createFailWhenColumnCountTooLarge() {
        List<String> seats = List.of("A1", "A2");

        assertThatThrownBy(() -> AuditoriumSeats.create(seats, 1, 51))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("열 수는 50을 초과할 수 없습니다.");
    }

    @Test
    @DisplayName("여러 어노테이션 검증 오류를 한 번에 반환한다")
    void createFailWhenMultipleBeanValidationErrors() {
        assertThatThrownBy(() -> AuditoriumSeats.create(List.of(), 0, 0))
                .isInstanceOf(AuditoriumDomainException.class)
                .satisfies(exception -> assertThat(
                        ((AuditoriumDomainException) exception).getErrors()
                ).contains(
                        "최소 하나 이상의 좌석이 필요합니다.",
                        "행 수는 0보다 커야 합니다.",
                        "열 수는 0보다 커야 합니다."
                ));
    }
}
