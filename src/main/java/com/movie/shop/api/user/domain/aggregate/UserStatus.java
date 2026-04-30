package com.movie.shop.api.user.domain.aggregate;

import com.movie.shop.api.shared.domain.EntityValidator;
import com.movie.shop.api.user.domain.exceptions.UserDomainException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@DiscriminatorColumn(
    name = "status",
    discriminatorType = DiscriminatorType.STRING,
    length = 30
)
public sealed abstract class UserStatus
    permits UserStatus.Active, UserStatus.Suspended, UserStatus.Withdrawn {

    @Column(name = "status", insertable = false, updatable = false)
    private String statusValue;

    protected UserStatus() {
    }

    @Embeddable
    @DiscriminatorValue("ACTIVE")
    @EqualsAndHashCode(callSuper = false)
    public static final class Active extends UserStatus {
        public Active() {
        }
    }

    @Embeddable
    @DiscriminatorValue("SUSPENDED")
    @Getter
    @EqualsAndHashCode(callSuper = false)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static final class Suspended extends UserStatus {

        @Embedded
        @Valid
        @NotNull(message = "정지 사유는 필수입니다.")
        @AttributeOverrides({
            @AttributeOverride(
                name = "code",
                column = @Column(name = "suspension_reason_code", length = 50)
            ),
            @AttributeOverride(
                name = "memo",
                column = @Column(name = "suspension_reason_memo", length = 500)
            )
        })
        private UserSuspensionReason reason;

        @NotNull(message = "정지 처리 운영자 ID는 필수입니다.")
        @Positive(message = "정지 처리 운영자 ID는 0보다 커야 합니다.")
        @Column(name = "suspended_by_operator_id")
        private Long suspendedByOperatorId;

        @NotNull(message = "정지 처리 시각은 필수입니다.")
        @Column(name = "suspended_at")
        private OffsetDateTime suspendedAt;

        public Suspended(
            UserSuspensionReason reason,
            long suspendedByOperatorId,
            OffsetDateTime suspendedAt
        ) {
            this.reason = reason;
            this.suspendedByOperatorId = suspendedByOperatorId;
            this.suspendedAt = suspendedAt;
            validate();
        }

        private void validate() {
            EntityValidator.create()
                .validateBean(this)
                .throwIfInvalid(UserDomainException::new);
        }
    }

    @Embeddable
    @DiscriminatorValue("WITHDRAWN")
    @EqualsAndHashCode(callSuper = false)
    public static final class Withdrawn extends UserStatus {
        public Withdrawn() {
        }
    }
}
