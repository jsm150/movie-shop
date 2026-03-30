package com.movie.shop.api.auditorium.domain.policy;

import com.movie.shop.api.auditorium.domain.aggregate.Auditorium;
import com.movie.shop.api.auditorium.domain.aggregate.AuditoriumStatusChange;
import com.movie.shop.api.auditorium.domain.exceptions.AuditoriumDomainException;
import com.movie.shop.api.auditorium.domain.port.CheckAuditoriumScreeningLinkPort;
import com.movie.shop.api.auditorium.domain.port.LoadAuditoriumTheaterActivationStatusPort;

import java.util.Objects;

public class AuditoriumStatusPolicy {

    private final CheckAuditoriumScreeningLinkPort checkAuditoriumScreeningLinkPort;
    private final LoadAuditoriumTheaterActivationStatusPort loadAuditoriumTheaterActivationStatusPort;

    public AuditoriumStatusPolicy(CheckAuditoriumScreeningLinkPort checkAuditoriumScreeningLinkPort,
                                  LoadAuditoriumTheaterActivationStatusPort loadAuditoriumTheaterActivationStatusPort) {
        this.checkAuditoriumScreeningLinkPort = Objects.requireNonNull(
                checkAuditoriumScreeningLinkPort,
                "상영 연결 조회 포트는 필수입니다."
        );
        this.loadAuditoriumTheaterActivationStatusPort = Objects.requireNonNull(
                loadAuditoriumTheaterActivationStatusPort,
                "영화관 활성 상태 조회 포트는 필수입니다."
        );
    }

    public void validateCanChangeStatus(Auditorium auditorium, AuditoriumStatusChange activeChange) {
        switch (activeChange) {
            case ACTIVATE -> validateCanActivate(auditorium);
            case DEACTIVATE -> validateCanDeactivate(auditorium);
        }
    }

    private void validateCanDeactivate(Auditorium auditorium) {
        if (!auditorium.isActive()) {
            return;
        }

        if (checkAuditoriumScreeningLinkPort.loadAuditoriumScreeningLinkStatus(auditorium.getId())) {
            throw new AuditoriumDomainException("예정/판매중/판매종료 상영이 존재하는 상영관은 비활성화할 수 없습니다.");
        }
    }

    private void validateCanActivate(Auditorium auditorium) {
        boolean theaterActive = loadAuditoriumTheaterActivationStatusPort
                .loadAuditoriumTheaterActivationStatus(auditorium.getTheaterId())
                .orElseThrow(() -> new AuditoriumDomainException("영화관 정보를 찾을 수 없습니다."));

        if (!theaterActive) {
            throw new AuditoriumDomainException("비활성화된 영화관의 상영관은 활성화할 수 없습니다.");
        }
    }
}
