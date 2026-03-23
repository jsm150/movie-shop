package com.movie.shop.api.auditorium.domain.aggregate;

import com.movie.shop.api.auditorium.domain.exceptions.AuditoriumDomainException;
import com.movie.shop.api.auditorium.domain.policy.AuditoriumDeletionPolicy;
import com.movie.shop.api.auditorium.domain.port.AuditoriumJpaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditoriumRepository {

    private final AuditoriumJpaPort auditoriumJpaPort;

    public Auditorium save(Auditorium auditorium) {
        return auditoriumJpaPort.save(auditorium);
    }

    public void delete(Auditorium auditorium, AuditoriumDeletionPolicy policy) {
        if (policy == null) {
            throw new AuditoriumDomainException("상영관 삭제 정책은 필수입니다.");
        }

        policy.validateCanDelete(auditorium);
        auditoriumJpaPort.delete(auditorium);
    }

    public long count() {
        return auditoriumJpaPort.count();
    }

    public Auditorium getById(long auditoriumId) {
        return auditoriumJpaPort.findById(auditoriumId)
                .orElseThrow(() -> new AuditoriumDomainException("상영관 데이터가 존재하지 않습니다."));
    }
}
