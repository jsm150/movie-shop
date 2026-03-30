package com.movie.shop.api.movie.domain.aggregate;


import com.movie.shop.api.movie.domain.policy.MovieTitlePolicy;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MovieTitle {

    private String title;

    public MovieTitle(String title) {
        this.title = title;
    }

    public static Validation<Seq<String>, MovieTitle> createNew(String title, MovieTitlePolicy validator) {
        return validateNotBlank(title)
                .flatMap(MovieTitle::validateLength)
                .flatMap(t -> validateNotDuplicate(t, validator))
                .map(MovieTitle::new)
                .mapError(List::of);
    }

    public static Validation<Seq<String>, MovieTitle> createFrom(MovieTitle nowTitle, String newTitle, MovieTitlePolicy validator) {
        return validateNotBlank(newTitle)
                .flatMap(MovieTitle::validateLength)
                .flatMap(t -> Option.of(t)
                        .filter(val -> !nowTitle.getTitle().equals(val))
                        .map(val -> validateNotDuplicate(val, validator))
                        .getOrElse(Validation.valid(t))
                )
                .map(MovieTitle::new)
                .mapError(List::of);
    }

    private static Validation<String, String> validateNotBlank(String title) {
        return title != null && !title.trim().isEmpty()
                ? Validation.valid(title)
                : Validation.invalid("영화 제목은 필수입니다.");
    }

    private static Validation<String, String> validateLength(String title) {
        return title.length() <= 200
                ? Validation.valid(title)
                : Validation.invalid("영화 제목은 200자를 초과할 수 없습니다.");
    }

    private static Validation<String, String> validateNotDuplicate(String title, MovieTitlePolicy validator) {
        validator.validateNotDuplicate(title);
        return Validation.valid(title);
    }

}
