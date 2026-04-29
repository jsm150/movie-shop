package com.movie.shop.api.screening.domain.aggregate;

import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;
import com.movie.shop.api.shared.domain.EntityValidator;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SalesTimeRange {

    @NotNull(message = "판매 시작 시간은 필수입니다.")
    @Column(name = "sales_start_at")
    private OffsetDateTime salesStartAt;

    @NotNull(message = "판매 종료 시간은 필수입니다.")
    @Column(name = "sales_end_at")
    private OffsetDateTime salesEndAt;

    private SalesTimeRange(OffsetDateTime salesStartAt, OffsetDateTime salesEndAt) {
        this.salesStartAt = salesStartAt;
        this.salesEndAt = salesEndAt;
        validate();
    }

    public static SalesTimeRange create(
            OffsetDateTime salesStartAt,
            OffsetDateTime salesEndAt,
            long auditoriumId,
            Long selfScreeningId,
            OffsetDateTime screeningStartAt,
            OffsetDateTime screeningEndAt,
            List<Screening> overlapCandidates
    ) {
        var salesTimeRange = new SalesTimeRange(salesStartAt, salesEndAt);
        validateAuditoriumId(auditoriumId);
        validateRequiredScreeningTimes(screeningStartAt, screeningEndAt);
        validateOverlapCandidatesRequired(overlapCandidates);
        salesTimeRange.validateBetween();
        salesTimeRange.validateSalesEndBeforeScreeningStart(screeningStartAt);
        validateNoConflict(
                overlapCandidates,
                selfScreeningId,
                screeningStartAt,
                screeningEndAt
        );
        return salesTimeRange;
    }

    private void validate() {
        EntityValidator.create()
                .validateBean(this)
                .throwIfInvalid(ScreeningDomainException::new);
    }

    private static void validateAuditoriumId(long auditoriumId) {
        if (auditoriumId <= 0) {
            throw new ScreeningDomainException("상영관 ID는 필수입니다.");
        }
    }

    private static void validateRequiredScreeningTimes(
            OffsetDateTime screeningStartAt,
            OffsetDateTime screeningEndAt
    ) {
        if (screeningStartAt == null) {
            throw new ScreeningDomainException("상영 시작 시간은 필수입니다.");
        }

        if (screeningEndAt == null) {
            throw new ScreeningDomainException("상영 종료 시간은 필수입니다.");
        }
    }

    private static void validateOverlapCandidatesRequired(
            List<Screening> overlapCandidates
    ) {
        if (overlapCandidates == null) {
            throw new ScreeningDomainException("상영 충돌 후보는 필수입니다.");
        }
    }

    private static void validateNoConflict(List<Screening> overlapCandidates,
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

    private void validateBetween() {
        if (!salesStartAt.isBefore(salesEndAt)) {
            throw new ScreeningDomainException(
                    "판매 시작 시간은 판매 종료 시간보다 이전이어야 합니다."
            );
        }
    }

    private void validateSalesEndBeforeScreeningStart(
            OffsetDateTime screeningStartAt
    ) {
        if (salesEndAt.isAfter(screeningStartAt)) {
            throw new ScreeningDomainException(
                    "판매 종료 시간은 상영 시작 시간보다 늦을 수 없습니다."
            );
        }
    }
}
