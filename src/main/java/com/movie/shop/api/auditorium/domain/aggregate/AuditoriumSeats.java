package com.movie.shop.api.auditorium.domain.aggregate;

import com.movie.shop.api.auditorium.domain.exceptions.AuditoriumDomainException;
import com.movie.shop.api.shared.domain.EntityValidator;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditoriumSeats {

    @NotEmpty(message = "최소 하나 이상의 좌석이 필요합니다.")
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<String> seats;

    @Min(value = 1, message = "행 수는 0보다 커야 합니다.")
    @Max(value = 100, message = "행 수는 100을 초과할 수 없습니다.")
    private int rowCount;

    @Min(value = 1, message = "열 수는 0보다 커야 합니다.")
    @Max(value = 50, message = "열 수는 50을 초과할 수 없습니다.")
    private int columnCount;

    private AuditoriumSeats(List<String> seats, int rowCount, int columnCount) {
        this.seats = seats;
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        validate();
    }

    public static AuditoriumSeats create(
            List<String> seats,
            int rowCount,
            int columnCount
    ) {
        return new AuditoriumSeats(seats, rowCount, columnCount);
    }

    private void validate() {
        EntityValidator.create()
                .validateBean(this)
                .throwIfInvalid(AuditoriumDomainException::new);
        validateSeatCount();
        validateDistinct();
    }

    private void validateSeatCount() {
        if (seats.size() != rowCount * columnCount) {
            throw new AuditoriumDomainException(
                    "좌석 수가 행과 열의 곱과 일치하지 않습니다."
            );
        }
    }

    private void validateDistinct() {
        if (seats.stream().distinct().count() != seats.size()) {
            throw new AuditoriumDomainException("중복된 좌석이 있습니다.");
        }
    }
}
