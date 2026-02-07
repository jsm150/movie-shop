package com.movie.shop.api.screening.domain.aggregate;

import com.movie.shop.api.shared.domain.ValidationUtils;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ScreeningTimeRange {
    @Column(name = "start_time")
    private OffsetDateTime startTime;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    private ScreeningTimeRange(OffsetDateTime startTime, OffsetDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static Validation<Seq<String>, ScreeningTimeRange> create(OffsetDateTime startTime, OffsetDateTime endTime) {
        return Validation.combine(
                ValidationUtils.notNull(startTime, "상영 시작 시간이 필요합니다."),
                ValidationUtils.notNull(endTime, "상영 종료 시간이 필요합니다.")
        )
                .ap(Tuple::of)
                .flatMap(tuple -> tuple.apply(ScreeningTimeRange::validateBetween)
                        .mapError(List::of))
                .map(tuple -> tuple.apply(ScreeningTimeRange::new));
    }

    private static Validation<String, Tuple2<OffsetDateTime, OffsetDateTime>> validateBetween(OffsetDateTime startTime, OffsetDateTime endTime) {
        return startTime.isBefore(endTime)
                ? Validation.valid(Tuple.of(startTime, endTime))
                : Validation.invalid("상영 시작 시간은 상영 종료 시간 이전 이여야 합니다.");
    }

    public boolean overlaps(OffsetDateTime targetStart, OffsetDateTime targetEnd) {
        return startTime.isBefore(targetEnd) && targetStart.isBefore(endTime);
    }

}
