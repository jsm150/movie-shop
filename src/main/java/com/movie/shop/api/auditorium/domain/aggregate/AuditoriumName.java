package com.movie.shop.api.auditorium.domain.aggregate;

import com.movie.shop.api.auditorium.domain.condition.AuditoriumNameUniquenessCondition;
import com.movie.shop.api.auditorium.domain.exceptions.AuditoriumDomainException;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditoriumName {

    private String name;

    public AuditoriumName(String name) {
        this.name = name;
    }

    public static Validation<Seq<String>, AuditoriumName> createNew(String name,
                                                                     AuditoriumNameUniquenessCondition nameCondition) {
        return validateNotBlank(name)
                .flatMap(AuditoriumName::validateLength)
                .flatMap(n -> validateNotDuplicate(n, nameCondition))
                .map(AuditoriumName::new)
                .mapError(List::of);
    }

    public static Validation<Seq<String>, AuditoriumName> createFrom(AuditoriumName nowName,
                                                                      String newName,
                                                                      AuditoriumNameUniquenessCondition nameCondition) {
        return validateNotBlank(newName)
                .flatMap(AuditoriumName::validateLength)
                .flatMap(n -> Option.of(n)
                        .filter(val -> !nowName.getName().equals(val))
                        .map(val -> validateNotDuplicate(val, nameCondition))
                        .getOrElse(Validation.valid(n))
                )
                .map(AuditoriumName::new)
                .mapError(List::of);
    }

    private static Validation<String, String> validateNotBlank(String name) {
        return name != null && !name.isEmpty()
                ? Validation.valid(name)
                : Validation.invalid("상영관 이름은 필수입니다.");
    }

    private static Validation<String, String> validateLength(String name) {
        return name.length() <= 50
                ? Validation.valid(name)
                : Validation.invalid("상영관 이름은 50자를 초과할 수 없습니다.");
    }

    private static Validation<String, String> validateNotDuplicate(String name,
                                                                   AuditoriumNameUniquenessCondition nameCondition) {
        if (nameCondition == null) {
            throw new AuditoriumDomainException("상영관 이름 중복 조건은 필수입니다.");
        }

        if (!nameCondition.unique()) {
            throw new AuditoriumDomainException("동일한 이름의 상영관이 해당 영화관에 이미 존재합니다.");
        }

        return Validation.valid(name);
    }
}
