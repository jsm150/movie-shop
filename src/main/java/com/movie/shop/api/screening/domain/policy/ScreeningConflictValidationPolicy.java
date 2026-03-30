package com.movie.shop.api.screening.domain.policy;

import com.movie.shop.api.screening.domain.aggregate.Screening;
import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;
import com.movie.shop.api.screening.domain.port.LoadScreeningConflictCandidatesPort;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public record ScreeningConflictValidationPolicy(LoadScreeningConflictCandidatesPort loadScreeningConflictCandidatesPort) {

    public ScreeningConflictValidationPolicy {
        Objects.requireNonNull(loadScreeningConflictCandidatesPort, "상영 충돌 후보 조회 포트는 필수입니다.");
    }

    public void validateNoConflict(long auditoriumId, OffsetDateTime screeningStart, OffsetDateTime screeningEnd) {
        validateConflict(loadConflictCandidates(auditoriumId, screeningStart, screeningEnd).stream(), screeningStart, screeningEnd);
    }

    public void validateNoConflictExcludingSelf(long auditoriumId,
                                                long selfScreeningId,
                                                OffsetDateTime screeningStart,
                                                OffsetDateTime screeningEnd) {
        validateConflict(
                excludeSelf(loadConflictCandidates(auditoriumId, screeningStart, screeningEnd), selfScreeningId),
                screeningStart,
                screeningEnd
        );
    }

    private List<Screening> loadConflictCandidates(long auditoriumId, OffsetDateTime screeningStart, OffsetDateTime screeningEnd) {
        return loadScreeningConflictCandidatesPort.loadConflictCandidates(auditoriumId, screeningStart, screeningEnd);
    }

    private Stream<Screening> excludeSelf(List<Screening> conflictCandidates, long selfScreeningId) {
        return conflictCandidates.stream()
                .filter(screening -> !Objects.equals(screening.getId(), selfScreeningId));
    }

    private void validateConflict(Stream<Screening> conflictCandidates,
                                  OffsetDateTime screeningStart,
                                  OffsetDateTime screeningEnd) {
        boolean hasConflict = conflictCandidates
                .anyMatch(screening -> screening.hasTimeConflictWith(screeningStart, screeningEnd));

        if (hasConflict) {
            throw new ScreeningDomainException("동일한 상영관에 상영 시간이 겹치는 일정이 존재합니다.");
        }
    }
}
