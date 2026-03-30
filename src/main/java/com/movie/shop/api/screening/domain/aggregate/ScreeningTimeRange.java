package com.movie.shop.api.screening.domain.aggregate;

import com.movie.shop.api.screening.domain.policy.ScreeningTimeRuntimeValidationPolicy;
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

    public static Validation<Seq<String>, ScreeningTimeRange> create(OffsetDateTime startTime,
                                                                      OffsetDateTime endTime,
                                                                      long movieId,
                                                                      ScreeningTimeRuntimeValidationPolicy runtimePolicy) {
        return Validation.combine(
                ValidationUtils.notNull(startTime, "상영 시작 시간이 필요합니다."),
                ValidationUtils.notNull(endTime, "상영 종료 시간이 필요합니다."),
                ValidationUtils.notNull(runtimePolicy, "상영 시간 런타임 검증 정책은 필수입니다.")
        )
                .ap(Tuple::of)
                .flatMap(tuple -> validateBetween(tuple._1, tuple._2)
                        .map(validRange -> validateRuntime(validRange, movieId, tuple._3)))
                .map(tuple -> tuple.apply(ScreeningTimeRange::new));
    }

    private static Validation<Seq<String>, Tuple2<OffsetDateTime, OffsetDateTime>> validateBetween(OffsetDateTime startTime, OffsetDateTime endTime) {
        return startTime.isBefore(endTime)
                ? Validation.valid(Tuple.of(startTime, endTime))
                : Validation.invalid(List.of("상영 시작 시간은 상영 종료 시간 이전 이여야 합니다."));
    }

    private static Tuple2<OffsetDateTime, OffsetDateTime> validateRuntime(Tuple2<OffsetDateTime, OffsetDateTime> validRange,
                                                                          long movieId,
                                                                          ScreeningTimeRuntimeValidationPolicy runtimePolicy) {
        runtimePolicy.validateRuntime(movieId, validRange._1, validRange._2);
        return validRange;
    }

    public boolean overlaps(OffsetDateTime targetStart, OffsetDateTime targetEnd) {
        return startTime.isBefore(targetEnd) && targetStart.isBefore(endTime);
    }

}
