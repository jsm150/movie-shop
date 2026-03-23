package com.movie.shop.api.theater.domain.aggregate;

import com.movie.shop.api.theater.domain.policy.TheaterNameDuplicateValidator;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TheaterName {

    private String name;

    public TheaterName(String name) {
        this.name = name;
    }

    public static Validation<Seq<String>, TheaterName> createNew(String name, TheaterNameDuplicateValidator validator) {
        return validateNotBlank(name)
                .flatMap(TheaterName::validateLength)
                .flatMap(n -> validateNotDuplicate(n, validator))
                .map(TheaterName::new)
                .mapError(List::of);
    }

    public static Validation<Seq<String>, TheaterName> createFrom(TheaterName nowName,
                                                                   String newName,
                                                                   TheaterNameDuplicateValidator validator) {
        return validateNotBlank(newName)
                .flatMap(TheaterName::validateLength)
                .flatMap(n -> Option.of(n)
                        .filter(val -> !nowName.getName().equals(val))
                        .map(val -> validateNotDuplicate(val, validator))
                        .getOrElse(Validation.valid(n))
                )
                .map(TheaterName::new)
                .mapError(List::of);
    }

    private static Validation<String, String> validateNotBlank(String name) {
        return name != null && !name.isEmpty()
                ? Validation.valid(name)
                : Validation.invalid("영화관 이름은 필수입니다.");
    }

    private static Validation<String, String> validateLength(String name) {
        return name.length() <= 50
                ? Validation.valid(name)
                : Validation.invalid("영화관 이름은 50자를 초과할 수 없습니다.");
    }

    private static Validation<String, String> validateNotDuplicate(String name, TheaterNameDuplicateValidator validator) {
        return validator.validateNotDuplicate(name)
                ? Validation.valid(name)
                : Validation.invalid("'" + name + "' 이름의 영화관이 이미 존재합니다.");
    }
}
