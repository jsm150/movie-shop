package com.movie.shop.api.movie.domain.aggregate;

import com.movie.shop.api.movie.domain.exceptions.MovieDomainException;
import com.movie.shop.api.shared.domain.EntityValidator;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Actor {

    @NotBlank(message = "배우 이름은 필수입니다.")
    @Size(max = 100, message = "배우 이름은 100자를 초과할 수 없습니다.")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotNull(message = "배우의 생년월일은 필수입니다.")
    @Past(message = "배우의 생년월일은 과거여야 합니다.")
    @Column(name = "date_of_birth", nullable = false)
    private OffsetDateTime dateOfBirth;

    @NotBlank(message = "배우의 국적은 필수입니다.")
    @Size(max = 100, message = "배우의 국적은 100자를 초과할 수 없습니다.")
    @Column(name = "national", nullable = false, length = 100)
    private String national;

    @NotBlank(message = "배우의 역할은 필수입니다.")
    @Size(max = 100, message = "배우의 역할은 100자를 초과할 수 없습니다.")
    @Column(name = "`role`", nullable = false, length = 100)
    private String role;

    public Actor(
        String name,
        OffsetDateTime dateOfBirth,
        String national,
        String role
    ) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.national = national;
        this.role = role;
        validate();
    }

    private void validate() {
        EntityValidator.create()
            .validateBean(this)
            .throwIfInvalid(MovieDomainException::new);
    }
}
