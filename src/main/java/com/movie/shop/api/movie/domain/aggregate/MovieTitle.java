package com.movie.shop.api.movie.domain.aggregate;

import com.movie.shop.api.movie.domain.condition.MovieTitleUniquenessCondition;
import com.movie.shop.api.movie.domain.exceptions.MovieDomainException;
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
public class MovieTitle {

    @NotBlank(message = "영화 제목은 필수입니다.")
    @Size(max = 200, message = "영화 제목은 200자를 초과할 수 없습니다.")
    private String title;

    private MovieTitle(String title) {
        this.title = title;
        validate();
    }

    public static MovieTitle createNew(
        String title,
        MovieTitleUniquenessCondition titleCondition
    ) {
        var movieTitle = new MovieTitle(title);
        validateNotDuplicate(titleCondition);
        return movieTitle;
    }

    public static MovieTitle createFrom(
        MovieTitle currentTitle,
        String newTitle,
        MovieTitleUniquenessCondition titleCondition
    ) {
        var movieTitle = new MovieTitle(newTitle);
        if (currentTitle == null || !currentTitle.hasSameTitle(newTitle)) {
            validateNotDuplicate(titleCondition);
        }
        return movieTitle;
    }

    private boolean hasSameTitle(String title) {
        return Objects.equals(this.title, title);
    }

    private void validate() {
        EntityValidator.create()
            .validateBean(this)
            .throwIfInvalid(MovieDomainException::new);
    }

    private static void validateNotDuplicate(
        MovieTitleUniquenessCondition titleCondition
    ) {
        if (titleCondition == null) {
            throw new MovieDomainException("영화 제목 중복 조건은 필수입니다.");
        }

        if (!titleCondition.unique()) {
            throw new MovieDomainException("동일한 제목의 영화가 이미 존재합니다.");
        }
    }
}
