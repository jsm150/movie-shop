package com.movie.shop.api.auditorium.domain.policy;

import com.movie.shop.api.auditorium.domain.port.AuditoriumJpaPort;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditoriumNameDuplicatePolicy {

    private final AuditoriumJpaPort auditoriumJpaPort;

    public boolean validateNotDuplicate(long theaterId, String name) {
        return !auditoriumJpaPort.existsByTheaterIdAndName(theaterId, name);
    }
}
