package com.movie.shop.api.theater.domain.aggregate;

import com.movie.shop.api.theater.domain.condition.TheaterAuditoriumPresence;
import com.movie.shop.api.theater.domain.exceptions.TheaterDomainException;
import com.movie.shop.api.theater.domain.port.TheaterJpaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TheaterRepository {

    private final TheaterJpaPort theaterJpaPort;

    public Theater save(Theater theater) {
        return theaterJpaPort.save(theater);
    }

    public void delete(Theater theater, TheaterAuditoriumPresence auditoriumPresence) {
        theater.validateCanDelete(auditoriumPresence);
        theaterJpaPort.delete(theater);
    }

    public Theater getById(long theaterId) {
        return theaterJpaPort.findById(theaterId)
                .orElseThrow(() -> new TheaterDomainException("영화관 데이터가 존재하지 않습니다."));
    }
}
