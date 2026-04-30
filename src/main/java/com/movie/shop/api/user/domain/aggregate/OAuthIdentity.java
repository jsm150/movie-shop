package com.movie.shop.api.user.domain.aggregate;

import com.movie.shop.api.shared.domain.EntityValidator;
import com.movie.shop.api.user.domain.condition.OAuthIdentityUniquenessCondition;
import com.movie.shop.api.user.domain.exceptions.UserDomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuthIdentity {

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", nullable = false, length = 30)
    @NotNull(message = "OAuth 제공자는 필수입니다.")
    private OAuthProvider provider;

    @Column(name = "oauth_provider_user_id", nullable = false, length = 255)
    @NotBlank(message = "OAuth 사용자 ID는 필수입니다.")
    @Size(max = 255, message = "OAuth 사용자 ID는 255자 이하여야 합니다.")
    private String providerUserId;

    private OAuthIdentity(OAuthProvider provider, String providerUserId) {
        this.provider = provider;
        this.providerUserId = providerUserId;
        validate();
    }

    public static OAuthIdentity create(OAuthProvider provider, String providerUserId) {
        return new OAuthIdentity(provider, providerUserId);
    }

    public static OAuthIdentity createNew(
        OAuthProvider provider,
        String providerUserId,
        OAuthIdentityUniquenessCondition uniquenessCondition
    ) {
        var oauthIdentity = new OAuthIdentity(provider, providerUserId);
        validateNotDuplicate(uniquenessCondition);
        return oauthIdentity;
    }

    private void validate() {
        EntityValidator.create()
                .validateBean(this)
                .throwIfInvalid(UserDomainException::new);
    }

    private static void validateNotDuplicate(
        OAuthIdentityUniquenessCondition uniquenessCondition
    ) {
        if (uniquenessCondition == null) {
            throw new UserDomainException("OAuth 식별자 중복 조건은 필수입니다.");
        }

        if (!uniquenessCondition.unique()) {
            throw new UserDomainException("동일한 OAuth 식별자의 사용자가 이미 존재합니다.");
        }
    }
}
