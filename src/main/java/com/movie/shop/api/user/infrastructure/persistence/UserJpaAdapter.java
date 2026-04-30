package com.movie.shop.api.user.infrastructure.persistence;

import com.movie.shop.api.user.domain.aggregate.OAuthProvider;
import com.movie.shop.api.user.domain.aggregate.User;
import com.movie.shop.api.user.domain.condition.OAuthIdentityUniquenessCondition;
import com.movie.shop.api.user.domain.port.OAuthIdentityUniquenessConditionPort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaAdapter extends JpaRepository<User, Long>, OAuthIdentityUniquenessConditionPort {

    boolean existsByOauthIdentity_ProviderAndOauthIdentity_ProviderUserId(
        OAuthProvider provider,
        String providerUserId
    );

    @Override
    default OAuthIdentityUniquenessCondition findCondition(
        OAuthProvider provider,
        String providerUserId
    ) {
        return new OAuthIdentityUniquenessCondition(
            !existsByOauthIdentity_ProviderAndOauthIdentity_ProviderUserId(
                provider,
                providerUserId
            )
        );
    }
}
