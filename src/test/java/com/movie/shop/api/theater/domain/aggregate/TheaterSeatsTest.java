package com.movie.shop.api.theater.domain.aggregate;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TheaterSeatsTest {

    @Test
    @DisplayName("좌석 생성 성공")
    void createSuccess() {
        List<String> seats = List.of("A1", "A2", "A3", "B1", "B2", "B3");

        Validation<Seq<String>, TheaterSeats> result = TheaterSeats.create(seats, 2, 3);

        assertTrue(result.isValid());
        TheaterSeats theaterSeats = result.get();
        assertEquals(seats, theaterSeats.getSeats());
        assertEquals(2, theaterSeats.getRowCount());
        assertEquals(3, theaterSeats.getColumnCount());
    }

    @Test
    @DisplayName("좌석 리스트가 비어 있으면 실패")
    void createFailWhenSeatsEmpty() {
        Validation<Seq<String>, TheaterSeats> result = TheaterSeats.create(List.of(), 2, 3);

        assertTrue(result.isInvalid());
        assertTrue(result.getError().contains("최소 하나 이상의 좌석이 필요합니다."));
    }

    @Test
    @DisplayName("좌석 수가 행*열과 다르면 실패")
    void createFailWhenSeatCountMismatch() {
        List<String> seats = List.of("A1", "A2", "A3");

        Validation<Seq<String>, TheaterSeats> result = TheaterSeats.create(seats, 2, 2);

        assertTrue(result.isInvalid());
        assertTrue(result.getError().contains("좌석 수가 행과 열의 곱과 일치하지 않습니다."));
    }

    @Test
    @DisplayName("중복된 좌석이 있으면 실패")
    void createFailWhenSeatDuplicated() {
        List<String> seats = List.of("A1", "A1", "A2", "A3");

        Validation<Seq<String>, TheaterSeats> result = TheaterSeats.create(seats, 2, 2);

        assertTrue(result.isInvalid());
        assertTrue(result.getError().contains("중복된 좌석이 있습니다."));
    }

    @Test
    @DisplayName("행 수가 0 이하면 실패")
    void createFailWhenRowCountTooSmall() {
        List<String> seats = List.of("A1", "A2");

        Validation<Seq<String>, TheaterSeats> result = TheaterSeats.create(seats, 0, 2);

        assertTrue(result.isInvalid());
        assertTrue(result.getError().contains("행 수는 0보다 커야 합니다."));
    }

    @Test
    @DisplayName("행 수가 100 초과면 실패")
    void createFailWhenRowCountTooLarge() {
        List<String> seats = List.of("A1", "A2");

        Validation<Seq<String>, TheaterSeats> result = TheaterSeats.create(seats, 101, 1);

        assertTrue(result.isInvalid());
        assertTrue(result.getError().contains("행 수는 100을 초과할 수 없습니다."));
    }

    @Test
    @DisplayName("열 수가 0 이하면 실패")
    void createFailWhenColumnCountTooSmall() {
        List<String> seats = List.of("A1", "A2");

        Validation<Seq<String>, TheaterSeats> result = TheaterSeats.create(seats, 1, 0);

        assertTrue(result.isInvalid());
        assertTrue(result.getError().contains("열 수는 0보다 커야 합니다."));
    }

    @Test
    @DisplayName("열 수가 50 초과면 실패")
    void createFailWhenColumnCountTooLarge() {
        List<String> seats = List.of("A1", "A2");

        Validation<Seq<String>, TheaterSeats> result = TheaterSeats.create(seats, 1, 51);

        assertTrue(result.isInvalid());
        assertTrue(result.getError().contains("열 수는 50을 초과할 수 없습니다."));
    }
}
