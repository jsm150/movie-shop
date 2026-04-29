package com.movie.shop.api.screening.domain.aggregate;

import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;
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
import java.util.Objects;
import java.util.stream.Stream;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SalesTimeRange {

    @Column(name = "sales_start_at")
    private OffsetDateTime salesStartAt;

    @Column(name = "sales_end_at")
    private OffsetDateTime salesEndAt;

    private SalesTimeRange(OffsetDateTime salesStartAt, OffsetDateTime salesEndAt) {
        this.salesStartAt = salesStartAt;
        this.salesEndAt = salesEndAt;
    }

    public static Validation<Seq<String>, SalesTimeRange> create(OffsetDateTime salesStartAt,
                                                                  OffsetDateTime salesEndAt,
                                                                  long auditoriumId,
                                                                  Long selfScreeningId,
                                                                  OffsetDateTime screeningStartAt,
                                                                  OffsetDateTime screeningEndAt,
                                                                  java.util.List<Screening> overlapCandidates) {
        return Validation.combine(
                ValidationUtils.notNull(salesStartAt, "판매 시작 시간은 필수입니다."),
                ValidationUtils.notNull(salesEndAt, "판매 종료 시간은 필수입니다."),
                ValidationUtils.notNull(auditoriumId > 0 ? auditoriumId : null, "상영관 ID는 필수입니다."),
                ValidationUtils.notNull(screeningStartAt, "상영 시작 시간은 필수입니다."),
                ValidationUtils.notNull(screeningEndAt, "상영 종료 시간은 필수입니다."),
                ValidationUtils.notNull(overlapCandidates, "상영 충돌 후보는 필수입니다.")
        )
                .ap(Tuple::of)
                .flatMap(tuple -> {
                    validateNoConflict(overlapCandidates, selfScreeningId, screeningStartAt, screeningEndAt);
                    return validate(salesStartAt, salesEndAt, screeningStartAt).mapError(List::of);
                })
                .map(tuple -> tuple.apply(SalesTimeRange::new));
    }

    private static void validateNoConflict(java.util.List<Screening> overlapCandidates,
                                           Long selfScreeningId,
                                           OffsetDateTime screeningStartAt,
                                           OffsetDateTime screeningEndAt) {
        Stream<Screening> conflictCandidates = overlapCandidates.stream();
        if (selfScreeningId != null) {
            conflictCandidates = conflictCandidates
                    .filter(screening -> !Objects.equals(screening.getId(), selfScreeningId));
        }

        boolean hasConflict = conflictCandidates
                .anyMatch(screening -> screening.hasTimeConflictWith(screeningStartAt, screeningEndAt));

        if (hasConflict) {
            throw new ScreeningDomainException("동일한 상영관에 상영 시간이 겹치는 일정이 존재합니다.");
        }
    }

    private static Validation<String, Tuple2<OffsetDateTime, OffsetDateTime>> validate(OffsetDateTime salesStartAt,
                                                                                        OffsetDateTime salesEndAt,
                                                                                        OffsetDateTime screeningStartAt) {
        if (!salesStartAt.isBefore(salesEndAt)) {
            return Validation.invalid("판매 시작 시간은 판매 종료 시간보다 이전이어야 합니다.");
        }

        if (salesEndAt.isAfter(screeningStartAt)) {
            return Validation.invalid("판매 종료 시간은 상영 시작 시간보다 늦을 수 없습니다.");
        }

        return Validation.valid(Tuple.of(salesStartAt, salesEndAt));
    }
}
