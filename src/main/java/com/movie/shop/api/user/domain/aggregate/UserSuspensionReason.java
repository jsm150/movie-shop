package com.movie.shop.api.user.domain.aggregate;

import com.movie.shop.api.shared.domain.EntityValidator;
import com.movie.shop.api.user.domain.exceptions.UserDomainException;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSuspensionReason {

    @Enumerated(EnumType.STRING)
    @NotNull(message = "정지 사유 코드는 필수입니다.")
    private UserSuspensionReasonCode code;

    @NotBlank(message = "정지 사유 메모는 필수입니다.")
    @Size(max = 500, message = "정지 사유 메모는 500자 이하여야 합니다.")
    private String memo;

    private UserSuspensionReason(UserSuspensionReasonCode code, String memo) {
        this.code = code;
        this.memo = memo;
        validate();
    }

    public static UserSuspensionReason create(
        UserSuspensionReasonCode code,
        String memo
    ) {
        return new UserSuspensionReason(code, memo);
    }

    void validate() {
        EntityValidator.create()
            .validateBean(this)
            .throwIfInvalid(UserDomainException::new);
    }
}
