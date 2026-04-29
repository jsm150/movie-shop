package com.movie.shop.api.auditorium.domain.aggregate;

import com.movie.shop.api.auditorium.domain.condition.AuditoriumNameUniquenessCondition;
import com.movie.shop.api.auditorium.domain.exceptions.AuditoriumDomainException;
import com.movie.shop.api.shared.domain.EntityValidator;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditoriumName {

    @NotBlank(message = "상영관 이름은 필수입니다.")
    @Size(max = 50, message = "상영관 이름은 50자를 초과할 수 없습니다.")
    private String name;

    private AuditoriumName(String name) {
        this.name = name;
        validate();
    }

    public static AuditoriumName createNew(
            String name,
            AuditoriumNameUniquenessCondition nameCondition
    ) {
        var auditoriumName = new AuditoriumName(name);
        validateNotDuplicate(nameCondition);
        return auditoriumName;
    }

    public static AuditoriumName createFrom(
            AuditoriumName currentName,
            String newName,
            AuditoriumNameUniquenessCondition nameCondition
    ) {
        var auditoriumName = new AuditoriumName(newName);
        if (currentName == null || !currentName.hasSameName(newName)) {
            validateNotDuplicate(nameCondition);
        }
        return auditoriumName;
    }

    public boolean hasSameName(String name) {
        return Objects.equals(this.name, name);
    }

    private void validate() {
        EntityValidator.create()
                .validateBean(this)
                .throwIfInvalid(AuditoriumDomainException::new);
    }

    private static void validateNotDuplicate(
            AuditoriumNameUniquenessCondition nameCondition
    ) {
        if (nameCondition == null) {
            throw new AuditoriumDomainException("상영관 이름 중복 조건은 필수입니다.");
        }

        if (!nameCondition.unique()) {
            throw new AuditoriumDomainException("동일한 이름의 상영관이 해당 영화관에 이미 존재합니다.");
        }
    }
}
