package com.movie.shop.api.screening.domain.aggregate;

import com.movie.shop.api.screening.domain.condition.MovieSchedulingCondition;
import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;
import com.movie.shop.api.shared.domain.EntityValidator;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ScreeningTimeRange {

    @NotNull(message = "상영 시작 시간이 필요합니다.")
    @Column(name = "start_time")
    private OffsetDateTime startTime;

    @NotNull(message = "상영 종료 시간이 필요합니다.")
    @Column(name = "end_time")
    private OffsetDateTime endTime;

    private ScreeningTimeRange(OffsetDateTime startTime, OffsetDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        validate();
    }

    public static ScreeningTimeRange create(
            OffsetDateTime startTime,
            OffsetDateTime endTime,
            MovieSchedulingCondition movieCondition
    ) {
        var timeRange = new ScreeningTimeRange(startTime, endTime);
        validateMovieCondition(movieCondition);
        timeRange.validateBetween();
        timeRange.validateRuntime(movieCondition);
        return timeRange;
    }

    private void validate() {
        EntityValidator.create()
                .validateBean(this)
                .throwIfInvalid(ScreeningDomainException::new);
    }

    private static void validateMovieCondition(MovieSchedulingCondition movieCondition) {
        if (movieCondition == null) {
            throw new ScreeningDomainException("영화 상영 조건은 필수입니다.");
        }
    }

    private void validateBetween() {
        if (!startTime.isBefore(endTime)) {
            throw new ScreeningDomainException(
                    "상영 시작 시간은 상영 종료 시간 이전 이여야 합니다."
            );
        }
    }

    private void validateRuntime(MovieSchedulingCondition movieCondition) {
        Duration screeningDuration = Duration.between(startTime, endTime);
        Duration runtimeDuration = Duration.ofMinutes(movieCondition.runtimeMinutes());

        if (screeningDuration.compareTo(runtimeDuration) < 0) {
            throw new ScreeningDomainException(
                    "상영 시간은 영화 런타임(%d분) 이상이어야 합니다.".formatted(movieCondition.runtimeMinutes())
            );
        }
    }

    public boolean overlaps(OffsetDateTime targetStart, OffsetDateTime targetEnd) {
        return startTime.isBefore(targetEnd) && targetStart.isBefore(endTime);
    }

}
