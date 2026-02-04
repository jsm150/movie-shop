package com.movie.shop.api.theater.domain.aggregate;

import io.vavr.Tuple;
import io.vavr.Tuple3;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TheaterSeats {

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<String> seats;

    private int rowCount;

    private int columnCount;

    private TheaterSeats(List<String> seats, int rowCount, int columnCount) {
        this.seats = seats;
        this.rowCount = rowCount;
        this.columnCount = columnCount;
    }

    public static Validation<Seq<String>, TheaterSeats> create(List<String> seats, int rowCount, int columnCount) {
        return Validation.combine(
                validateSeatsNotEmpty(seats)
                        .flatMap(s -> validateSeatCount(s, rowCount, columnCount))
                        .flatMap(TheaterSeats::validateDistinct),
                validateRowCount(rowCount),
                validateColumnCount(columnCount)
            ).ap(TheaterSeats::new);
    }

    private static Validation<String, List<String>> validateSeatsNotEmpty(List<String> seats) {
        return seats != null && !seats.isEmpty()
                ? Validation.valid(seats)
                : Validation.invalid("최소 하나 이상의 좌석이 필요합니다.");
    }

    private static Validation<String, Integer> validateRowCount(int rowCount) {
        if (rowCount <= 0) {
            return Validation.invalid("행 수는 0보다 커야 합니다.");
        }
        if (rowCount > 100) {
            return Validation.invalid("행 수는 100을 초과할 수 없습니다.");
        }
        return Validation.valid(rowCount);
    }

    private static Validation<String, Integer> validateColumnCount(int columnCount) {
        if (columnCount <= 0) {
            return Validation.invalid("열 수는 0보다 커야 합니다.");
        }
        if (columnCount > 50) {
            return Validation.invalid("열 수는 50을 초과할 수 없습니다.");
        }
        return Validation.valid(columnCount);
    }

    private static Validation<String, List<String>> validateSeatCount(List<String> seats, int rowCount, int columnCount) {
        return seats.size() == rowCount * columnCount
                ? Validation.valid(seats)
                : Validation.invalid("좌석 수가 행과 열의 곱과 일치하지 않습니다.");
    }

    private static Validation<String, List<String>> validateDistinct(List<String> seats) {
        return seats.stream().distinct().count() == seats.size()
                ? Validation.valid(seats)
                : Validation.invalid("중복된 좌석이 있습니다.");

    }
}
