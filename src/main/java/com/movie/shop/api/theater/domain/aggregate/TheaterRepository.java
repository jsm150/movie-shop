package com.movie.shop.api.theater.domain.aggregate;

import com.movie.shop.api.theater.domain.port.TheaterJpaPort;
import com.movie.shop.api.theater.domain.exceptions.TheaterDomainException;
import com.movie.shop.api.theater.domain.policy.TheaterScreeningProtectionPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TheaterRepository {

    private final TheaterJpaPort theaterJpaPort;

    public Theater save(Theater theater) {
        return theaterJpaPort.save(theater);
    }

    public void delete(Theater theater, TheaterScreeningProtectionPolicy policy) {
        if (policy == null) {
            throw new TheaterDomainException("상영관 삭제 정책은 필수입니다.");
        }

        policy.validateCanDelete(theater);
        theaterJpaPort.delete(theater);
    }

    public long count() {
        return theaterJpaPort.count();
    }

    public boolean existsByName(String name) {
        return theaterJpaPort.existsByName(name);
    }

    public Theater getById(long theaterId) {
        return theaterJpaPort.findById(theaterId)
                .orElseThrow(() -> new TheaterDomainException("상영관 데이터가 존재하지 않습니다."));
    }
}
