package com.movie.shop.api.theater.domain.aggregate;

import com.movie.shop.api.theater.domain.condition.TheaterNameUniquenessCondition;
import com.movie.shop.api.theater.domain.exceptions.TheaterDomainException;
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
public class TheaterName {

    @NotBlank(message = "영화관 이름은 필수입니다.")
    @Size(max = 50, message = "영화관 이름은 50자를 초과할 수 없습니다.")
    private String name;

    private TheaterName(String name) {
        this.name = name;
        validate();
    }

    public static TheaterName createNew(
            String name,
            TheaterNameUniquenessCondition nameCondition
    ) {
        var theaterName = new TheaterName(name);
        validateNotDuplicate(nameCondition);
        return theaterName;
    }

    public static TheaterName createFrom(
            TheaterName currentName,
            String newName,
            TheaterNameUniquenessCondition nameCondition
    ) {
        var theaterName = new TheaterName(newName);
        if (currentName == null || !currentName.hasSameName(newName)) {
            validateNotDuplicate(nameCondition);
        }
        return theaterName;
    }

    private boolean hasSameName(String name) {
        return Objects.equals(this.name, name);
    }

    private void validate() {
        EntityValidator.create()
                .validateBean(this)
                .throwIfInvalid(TheaterDomainException::new);
    }

    private static void validateNotDuplicate(TheaterNameUniquenessCondition nameCondition) {
        if (nameCondition == null) {
            throw new TheaterDomainException("영화관 이름 중복 조건은 필수입니다.");
        }

        if (!nameCondition.unique()) {
            throw new TheaterDomainException("동일한 이름의 영화관이 이미 존재합니다.");
        }
    }
}
