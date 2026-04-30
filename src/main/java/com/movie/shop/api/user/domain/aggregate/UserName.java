package com.movie.shop.api.user.domain.aggregate;

import com.movie.shop.api.shared.domain.EntityValidator;
import com.movie.shop.api.user.domain.exceptions.UserDomainException;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserName {

    @NotBlank(message = "사용자 이름은 필수입니다.")
    @Size(max = 100, message = "사용자 이름은 100자 이하여야 합니다.")
    private String name;

    private UserName(String name) {
        this.name = name;
        validate();
    }

    public static UserName create(String name) {
        return new UserName(name);
    }

    private void validate() {
        EntityValidator.create()
                .validateBean(this)
                .throwIfInvalid(UserDomainException::new);
    }
}
