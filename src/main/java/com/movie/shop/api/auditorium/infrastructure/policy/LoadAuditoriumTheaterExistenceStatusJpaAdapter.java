package com.movie.shop.api.auditorium.infrastructure.policy;

import com.movie.shop.api.auditorium.domain.port.LoadAuditoriumTheaterExistenceStatusPort;
import com.movie.shop.api.theater.domain.port.TheaterJpaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoadAuditoriumTheaterExistenceStatusJpaAdapter implements LoadAuditoriumTheaterExistenceStatusPort {

    private final TheaterJpaPort theaterJpaPort;

    @Override
    public boolean loadAuditoriumTheaterExistenceStatus(long theaterId) {
        return theaterJpaPort.findById(theaterId).isPresent();
    }
}
