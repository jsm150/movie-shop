package com.movie.shop.api.user.domain.aggregate;

import com.movie.shop.api.shared.domain.EntityValidator;
import com.movie.shop.api.user.domain.exceptions.UserDomainException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UserEmail {

    @NotBlank(message = "이메일은 필수입니다.")
    @Size(max = 255, message = "이메일은 255자 이하여야 합니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private final String email;

    private UserEmail(String email) {
        this.email = email;
        validate();
    }

    public static UserEmail create(String email) {
        return new UserEmail(email);
    }

    private void validate() {
        EntityValidator.create()
            .validateBean(this)
            .throwIfInvalid(UserDomainException::new);
    }
}
