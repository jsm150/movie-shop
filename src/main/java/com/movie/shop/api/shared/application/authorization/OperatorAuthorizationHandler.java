package com.movie.shop.api.shared.application.authorization;

public interface OperatorAuthorizationHandler<T extends OperatorAuthorizableCommand<?>> {

    Class<?> supports();

    void authorize(long operatorId, T command);
}
