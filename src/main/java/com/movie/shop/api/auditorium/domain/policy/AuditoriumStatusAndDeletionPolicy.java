package com.movie.shop.api.auditorium.domain.policy;

import com.movie.shop.api.auditorium.domain.aggregate.Auditorium;
import com.movie.shop.api.auditorium.domain.aggregate.AuditoriumStatusChange;
import com.movie.shop.api.auditorium.domain.exceptions.AuditoriumDomainException;
import com.movie.shop.api.auditorium.domain.policy.status.AuditoriumScreeningLinkStatus;
import com.movie.shop.api.auditorium.domain.policy.status.AuditoriumTheaterActivationStatus;

import java.util.Objects;
import java.util.Optional;

public class AuditoriumStatusAndDeletionPolicy {

    private final AuditoriumScreeningLinkStatus screeningLinkStatus;
    private final Optional<AuditoriumTheaterActivationStatus> theaterActivationStatus;

    public AuditoriumStatusAndDeletionPolicy(AuditoriumScreeningLinkStatus screeningLinkStatus,
                                             Optional<AuditoriumTheaterActivationStatus> theaterActivationStatus) {
        this.screeningLinkStatus = Objects.requireNonNull(screeningLinkStatus, "상영 연결 상태는 필수입니다.");
        this.theaterActivationStatus = Objects.requireNonNull(theaterActivationStatus, "영화관 활성 상태 정보는 필수입니다.");
    }

    public void validateCanChangeStatus(Auditorium auditorium, AuditoriumStatusChange activeChange) {
        switch (activeChange) {
            case ACTIVATE -> validateCanActivate();
            case DEACTIVATE -> validateCanDeactivate(auditorium);
        }
    }

    public void validateCanDelete(Auditorium auditorium) {
        if (screeningLinkStatus.blockingScreeningExists()) {
            throw new AuditoriumDomainException("예정/판매중/판매종료 상영이 존재하는 상영관은 삭제할 수 없습니다.");
        }
    }

    private void validateCanDeactivate(Auditorium auditorium) {
        if (!auditorium.isActive()) {
            return;
        }

        if (screeningLinkStatus.blockingScreeningExists()) {
            throw new AuditoriumDomainException("예정/판매중/판매종료 상영이 존재하는 상영관은 비활성화할 수 없습니다.");
        }
    }

    private void validateCanActivate() {
        AuditoriumTheaterActivationStatus theaterStatus = theaterActivationStatus
                .orElseThrow(() -> new AuditoriumDomainException("영화관 정보를 찾을 수 없습니다."));

        if (!theaterStatus.active()) {
            throw new AuditoriumDomainException("비활성화된 영화관의 상영관은 활성화할 수 없습니다.");
        }
    }
}
