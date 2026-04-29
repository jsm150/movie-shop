package com.movie.shop.api.movie.domain.aggregate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.movie.shop.api.movie.domain.exceptions.MovieDomainException;
import com.movie.shop.api.shared.domain.EntityValidator;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Actor {

    @NotBlank(message = "배우 이름은 필수입니다.")
    @Size(max = 100, message = "배우 이름은 100자를 초과할 수 없습니다.")
    private String name;

    @NotNull(message = "배우의 생년월일은 필수입니다.")
    @Past(message = "배우의 생년월일은 과거여야 합니다.")
    private OffsetDateTime dateOfBirth;

    @NotBlank(message = "배우의 국적은 필수입니다.")
    @Size(max = 100, message = "배우의 국적은 100자를 초과할 수 없습니다.")
    private String national;

    @NotBlank(message = "배우의 역할은 필수입니다.")
    @Size(max = 100, message = "배우의 역할은 100자를 초과할 수 없습니다.")
    private String role;

    @JsonCreator
    public Actor(
        @JsonProperty("name") String name,
        @JsonProperty("dateOfBirth") OffsetDateTime dateOfBirth,
        @JsonProperty("national") String national,
        @JsonProperty("role") String role
    ) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.national = national;
        this.role = role;

        // 어노테이션 검증 수행
        EntityValidator.create()
            .validateBean(this)
            .throwIfInvalid(MovieDomainException::new);
    }
}
