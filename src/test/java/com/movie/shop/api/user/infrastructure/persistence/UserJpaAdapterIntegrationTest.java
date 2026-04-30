package com.movie.shop.api.user.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.movie.shop.api.configuration.AbstractContainerBase;
import com.movie.shop.api.user.domain.aggregate.OAuthProvider;
import com.movie.shop.api.user.domain.aggregate.User;
import com.movie.shop.api.user.domain.aggregate.UserStatus;
import com.movie.shop.api.user.domain.aggregate.UserSuspensionReason;
import com.movie.shop.api.user.domain.aggregate.UserSuspensionReasonCode;
import com.movie.shop.api.user.domain.condition.OAuthIdentityUniquenessCondition;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class UserJpaAdapterIntegrationTest extends AbstractContainerBase {

    private static final OffsetDateTime SUSPENDED_AT =
        OffsetDateTime.parse("2026-04-30T10:00:00Z");

    @Autowired
    private UserJpaAdapter userJpaAdapter;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @Transactional
    @DisplayName("정지 유저 상태는 사유와 처리자와 처리 시각을 보존한다")
    void saveAndLoad_withSuspendedUser_preservesSuspensionDetails() {
        User user = registerUser("google-sub-suspended");
        user.suspend(suspensionReason(), 7L, SUSPENDED_AT);

        User saved = userJpaAdapter.save(user);

        entityManager.flush();
        entityManager.clear();

        User loaded = userJpaAdapter.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getStatus()).isInstanceOfSatisfying(
            UserStatus.Suspended.class,
            status -> {
                assertThat(status.getReason().getCode()).isEqualTo(UserSuspensionReasonCode.POLICY_VIOLATION);
                assertThat(status.getReason().getMemo()).isEqualTo("약관 위반");
                assertThat(status.getSuspendedByOperatorId()).isEqualTo(7L);
                assertThat(status.getSuspendedAt()).isEqualTo(SUSPENDED_AT);
            }
        );
    }

    @Test
    @Transactional
    @DisplayName("정지 해제 후 저장하면 현재 상태의 정지 상세는 제거된다")
    void saveAndLoad_afterActivate_clearsSuspensionDetails() {
        User user = registerUser("google-sub-activated");
        user.suspend(suspensionReason(), 7L, SUSPENDED_AT);
        user.activate();

        User saved = userJpaAdapter.save(user);

        entityManager.flush();
        entityManager.clear();

        User loaded = userJpaAdapter.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getStatus()).isEqualTo(new UserStatus.Active());

        Object[] row = (Object[]) entityManager.createNativeQuery("""
                SELECT status, suspension_reason_code, suspension_reason_memo, suspended_by_operator_id, suspended_at
                FROM user_account
                WHERE user_id = :userId
                """)
            .setParameter("userId", saved.getId())
            .getSingleResult();

        assertThat(row[0]).isEqualTo("ACTIVE");
        assertThat(row[1]).isNull();
        assertThat(row[2]).isNull();
        assertThat(row[3]).isNull();
        assertThat(row[4]).isNull();
    }

    private User registerUser(String providerUserId) {
        return User.registerWithOAuth(
            new OAuthIdentityUniquenessCondition(true),
            OAuthProvider.GOOGLE,
            providerUserId,
            Optional.of("user@example.com"),
            "User"
        );
    }

    private UserSuspensionReason suspensionReason() {
        return UserSuspensionReason.create(
            UserSuspensionReasonCode.POLICY_VIOLATION,
            "약관 위반"
        );
    }
}
