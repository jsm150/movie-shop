package com.movie.shop.api.auditorium.domain.aggregate;

import com.movie.shop.api.auditorium.domain.condition.AuditoriumScreeningPresence;
import com.movie.shop.api.auditorium.domain.exceptions.AuditoriumDomainException;
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

    public void delete(Auditorium auditorium, AuditoriumScreeningPresence screeningPresence) {
        auditorium.validateCanDelete(screeningPresence);
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
