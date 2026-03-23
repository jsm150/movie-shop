package com.movie.shop.api.auditorium.domain.aggregate;

import com.movie.shop.api.auditorium.domain.policy.AuditoriumNameDuplicatePolicy;
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

    public static Validation<Seq<String>, AuditoriumName> createNew(long theaterId,
                                                                     String name,
                                                                     AuditoriumNameDuplicatePolicy validator) {
        return validateNotBlank(name)
                .flatMap(AuditoriumName::validateLength)
                .flatMap(n -> validateNotDuplicate(theaterId, n, validator))
                .map(AuditoriumName::new)
                .mapError(List::of);
    }

    public static Validation<Seq<String>, AuditoriumName> createFrom(long theaterId,
                                                                      AuditoriumName nowName,
                                                                      String newName,
                                                                      AuditoriumNameDuplicatePolicy validator) {
        return validateNotBlank(newName)
                .flatMap(AuditoriumName::validateLength)
                .flatMap(n -> Option.of(n)
                        .filter(val -> !nowName.getName().equals(val))
                        .map(val -> validateNotDuplicate(theaterId, val, validator))
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

    private static Validation<String, String> validateNotDuplicate(long theaterId,
                                                                    String name,
                                                                    AuditoriumNameDuplicatePolicy validator) {
        return validator.validateNotDuplicate(theaterId, name)
                ? Validation.valid(name)
                : Validation.invalid("'" + name + "' 이름의 상영관이 해당 영화관에 이미 존재합니다.");
    }
}
