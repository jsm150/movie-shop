package com.movie.shop.api.screening.domain.aggregate;

import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;
import com.movie.shop.api.screening.domain.policy.ScreeningScheduleValidationPolicy;
import com.movie.shop.api.screening.domain.policy.ScreeningTimeRuntimeValidationPolicy;
import com.movie.shop.api.shared.domain.EntityValidator;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "screening",
        indexes = {
                @Index(name = "idx_screening_theater_end_start", columnList = "theater_id,end_time,start_time"),
                @Index(name = "idx_screening_movie_start", columnList = "movie_id,start_time")
        }
)
public class Screening {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, name = "screening_id")
    private Long id;

    @Positive(message = "영화 ID는 0보다 커야 합니다.")
    @Column(nullable = false, name = "movie_id")
    private long movieId;

    @Positive(message = "극장 ID는 0보다 커야 합니다.")
    @Column(nullable = false, name = "theater_id")
    private long theaterId;

    @Setter(AccessLevel.PRIVATE)
    @NotNull(message = "상영 시간 범위는 필수입니다.")
    @Embedded
    private ScreeningTimeRange screeningTimeRange;

    @Setter(AccessLevel.PRIVATE)
    @NotNull(message = "판매 시간 범위는 필수입니다.")
    @Embedded
    private SalesTimeRange salesTimeRange;

    @NotNull(message = "상영 상태는 필수입니다.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ScreeningStatus status;

    @Column(name = "canceled_at")
    private OffsetDateTime canceledAt;

    @Size(max = 200, message = "취소 사유는 200자 이하여야 합니다.")
    @Column(name = "cancel_reason", length = 200)
    private String cancelReason;

    public static Screening register(ScreeningScheduleValidationPolicy schedulePolicy,
                                     ScreeningTimeRuntimeValidationPolicy runtimePolicy,
                                     long movieId,
                                     long theaterId,
                                     OffsetDateTime screeningStart,
                                     OffsetDateTime screeningEnd,
                                     OffsetDateTime salesStart,
                                     OffsetDateTime salesEnd) {
        if (schedulePolicy == null) {
            throw new ScreeningDomainException("상영 일정 검증 정책은 필수입니다.");
        }
        if (runtimePolicy == null) {
            throw new ScreeningDomainException("상영 시간 런타임 검증 정책은 필수입니다.");
        }

        schedulePolicy.validateCanCreateScreeningSchedule(screeningStart, screeningEnd);

        var screening = new Screening();
        screening.movieId = movieId;
        screening.theaterId = theaterId;
        screening.status = ScreeningStatus.SCHEDULED;

        EntityValidator.create()
                .apply(ScreeningTimeRange.create(screeningStart, screeningEnd, runtimePolicy), screening::setScreeningTimeRange)
                .apply(SalesTimeRange.create(salesStart, salesEnd, screeningStart), screening::setSalesTimeRange)
                .validateBean(screening)
                .throwIfInvalid(ScreeningDomainException::new);

        return screening;
    }

    public void reschedule(ScreeningScheduleValidationPolicy schedulePolicy,
                           ScreeningTimeRuntimeValidationPolicy runtimePolicy,
                           OffsetDateTime screeningStart,
                           OffsetDateTime screeningEnd,
                           OffsetDateTime salesStart,
                           OffsetDateTime salesEnd) {
        if (schedulePolicy == null) {
            throw new ScreeningDomainException("상영 일정 검증 정책은 필수입니다.");
        }
        if (runtimePolicy == null) {
            throw new ScreeningDomainException("상영 시간 런타임 검증 정책은 필수입니다.");
        }

        if (this.id == null) {
            throw new ScreeningDomainException("상영 ID가 존재하지 않아 일정 변경 검증을 수행할 수 없습니다.");
        }

        schedulePolicy.validateCanRescheduleScreening(screeningStart, screeningEnd);

        if (status != ScreeningStatus.SCHEDULED) {
            throw new ScreeningDomainException("SCHEDULED 상태의 상영만 일정 변경이 가능합니다.");
        }

        EntityValidator.create()
                .apply(ScreeningTimeRange.create(screeningStart, screeningEnd, runtimePolicy), this::setScreeningTimeRange)
                .apply(SalesTimeRange.create(salesStart, salesEnd, screeningStart), this::setSalesTimeRange)
                .validateBean(this)
                .throwIfInvalid(ScreeningDomainException::new);
    }

    public void openSales(OffsetDateTime now) {
        if (status != ScreeningStatus.SCHEDULED) {
            throw new ScreeningDomainException("판매 시작은 SCHEDULED 상태에서만 가능합니다.");
        }

        if (now == null) {
            throw new ScreeningDomainException("현재 시간은 필수입니다.");
        }

        if (now.isBefore(salesTimeRange.getSalesStartAt())) {
            throw new ScreeningDomainException("판매 시작 시간 이전에는 판매를 시작할 수 없습니다.");
        }

        status = ScreeningStatus.ON_SALE;
    }

    public void closeSales() {
        if (status != ScreeningStatus.ON_SALE) {
            throw new ScreeningDomainException("판매 종료는 ON_SALE 상태에서만 가능합니다.");
        }

        status = ScreeningStatus.SALES_CLOSED;
    }

    public void cancel(String reason, OffsetDateTime now) {
        if (status == ScreeningStatus.CANCELED || status == ScreeningStatus.FINISHED) {
            throw new ScreeningDomainException("종료 상태의 상영은 취소할 수 없습니다.");
        }

        if (reason == null || reason.isBlank()) {
            throw new ScreeningDomainException("취소 사유는 필수입니다.");
        }

        if (reason.length() > 200) {
            throw new ScreeningDomainException("취소 사유는 200자 이하여야 합니다.");
        }

        if (now == null) {
            throw new ScreeningDomainException("현재 시간은 필수입니다.");
        }

        cancelReason = reason;
        canceledAt = now;
        status = ScreeningStatus.CANCELED;
    }

    public void finish(OffsetDateTime now) {
        if (status != ScreeningStatus.SALES_CLOSED) {
            throw new ScreeningDomainException("SALES_CLOSED 상태의 상영만 종료할 수 있습니다.");
        }

        if (now == null) {
            throw new ScreeningDomainException("현재 시간은 필수입니다.");
        }

        if (now.isBefore(screeningTimeRange.getEndTime())) {
            throw new ScreeningDomainException("상영 종료 시간 이전에는 상영을 종료할 수 없습니다.");
        }

        status = ScreeningStatus.FINISHED;
    }

    public void changeState(ScreeningStateChange stateChange, String reason, OffsetDateTime now) {
        if (stateChange == null) {
            throw new ScreeningDomainException("변경할 상영 상태는 필수입니다.");
        }

        switch (stateChange) {
            case OPEN_SALES -> openSales(now);
            case CLOSE_SALES -> closeSales();
            case CANCEL -> cancel(reason, now);
            case FINISH -> finish(now);
        }
    }

    public void validateCanRemove() {
        if (status != ScreeningStatus.SCHEDULED) {
            throw new ScreeningDomainException("SCHEDULED 상태의 상영만 삭제할 수 있습니다.");
        }
    }

    public boolean blocksTheaterDeactivationOrDeletion() {
        return status == ScreeningStatus.SCHEDULED
                || status == ScreeningStatus.ON_SALE
                || status == ScreeningStatus.SALES_CLOSED;
    }

    public boolean hasTimeConflictWith(OffsetDateTime targetStart, OffsetDateTime targetEnd) {
        if (status == ScreeningStatus.CANCELED) {
            return false;
        }

        return screeningTimeRange.overlaps(targetStart, targetEnd);
    }
}
