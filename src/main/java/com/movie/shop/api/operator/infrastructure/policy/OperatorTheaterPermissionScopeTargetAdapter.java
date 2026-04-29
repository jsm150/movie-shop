package com.movie.shop.api.operator.infrastructure.policy;

import com.movie.shop.api.operator.domain.condition.OperatorTheaterPermissionScopeTarget;
import com.movie.shop.api.operator.domain.port.OperatorTheaterPermissionScopeTargetPort;
import com.movie.shop.api.theater.domain.port.TheaterJpaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OperatorTheaterPermissionScopeTargetAdapter implements OperatorTheaterPermissionScopeTargetPort {

    private final TheaterJpaPort theaterJpaPort;

    @Override
    public Optional<OperatorTheaterPermissionScopeTarget> findScopeTarget(long theaterId) {
        return theaterJpaPort.findById(theaterId)
                .map(theater -> new OperatorTheaterPermissionScopeTarget(theater.getId()));
    }
}
