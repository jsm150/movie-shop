package com.movie.shop.api.user.domain.port;

import com.movie.shop.api.user.domain.aggregate.OAuthProvider;
import com.movie.shop.api.user.domain.condition.OAuthIdentityUniquenessCondition;

public interface OAuthIdentityUniquenessConditionPort {

    OAuthIdentityUniquenessCondition findCondition(
        OAuthProvider provider,
        String providerUserId
    );
}
