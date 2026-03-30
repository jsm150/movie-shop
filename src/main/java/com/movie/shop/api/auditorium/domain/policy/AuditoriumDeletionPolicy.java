package com.movie.shop.api.auditorium.domain.policy;

import com.movie.shop.api.auditorium.domain.aggregate.Auditorium;
import com.movie.shop.api.auditorium.domain.exceptions.AuditoriumDomainException;
import com.movie.shop.api.auditorium.domain.port.CheckAuditoriumScreeningLinkPort;

import java.util.Objects;

public class AuditoriumDeletionPolicy {

    private final CheckAuditoriumScreeningLinkPort checkAuditoriumScreeningLinkPort;

    public AuditoriumDeletionPolicy(CheckAuditoriumScreeningLinkPort checkAuditoriumScreeningLinkPort) {
        this.checkAuditoriumScreeningLinkPort = Objects.requireNonNull(
                checkAuditoriumScreeningLinkPort,
                "상영 연결 조회 포트는 필수입니다."
        );
    }

    public void validateCanDelete(Auditorium auditorium) {
        if (checkAuditoriumScreeningLinkPort.loadAuditoriumScreeningLinkStatus(auditorium.getId())) {
            throw new AuditoriumDomainException("예정/판매중/판매종료 상영이 존재하는 상영관은 삭제할 수 없습니다.");
        }
    }
}
