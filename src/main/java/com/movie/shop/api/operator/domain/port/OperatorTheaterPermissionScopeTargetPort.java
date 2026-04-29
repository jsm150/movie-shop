package com.movie.shop.api.operator.domain.port;

import com.movie.shop.api.operator.domain.condition.OperatorTheaterPermissionScopeTarget;

import java.util.Optional;

public interface OperatorTheaterPermissionScopeTargetPort {

    Optional<OperatorTheaterPermissionScopeTarget> findScopeTarget(long theaterId);
}
