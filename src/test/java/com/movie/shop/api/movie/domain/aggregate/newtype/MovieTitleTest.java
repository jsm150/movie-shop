package com.movie.shop.api.movie.domain.aggregate.newtype;

import com.movie.shop.api.movie.domain.aggregate.MovieTitle;
import com.movie.shop.api.movie.domain.condition.MovieTitleUniquenessCondition;
import com.movie.shop.api.movie.domain.exceptions.MovieDomainException;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("MovieTitle 단위 테스트")
class MovieTitleTest {

    private MovieTitleUniquenessCondition uniqueCondition;
    private MovieTitleUniquenessCondition duplicateCondition;

    @BeforeEach
    void setUp() {
        uniqueCondition = new MovieTitleUniquenessCondition(true);
        duplicateCondition = new MovieTitleUniquenessCondition(false);
    }

    @Nested
    @DisplayName("createNew 메서드 테스트")
    class CreateNewTest {

        @Test
        @DisplayName("유효한 제목으로 생성 성공한다")
        void createNew_withValidTitle_success() {
            String validTitle = "인터스텔라";

            Validation<Seq<String>, MovieTitle> result = MovieTitle.createNew(validTitle, uniqueCondition);

            assertThat(result.isValid()).isTrue();
            assertThat(result.get().getTitle()).isEqualTo(validTitle);
        }

        @Test
        @DisplayName("null 제목으로 생성 실패한다")
        void createNew_withNullTitle_fail() {
            Validation<Seq<String>, MovieTitle> result = MovieTitle.createNew(null, uniqueCondition);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("영화 제목은 필수입니다.");
        }

        @Test
        @DisplayName("빈 문자열 제목으로 생성 실패한다")
        void createNew_withEmptyTitle_fail() {
            Validation<Seq<String>, MovieTitle> result = MovieTitle.createNew("", uniqueCondition);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("영화 제목은 필수입니다.");
        }

        @Test
        @DisplayName("200자를 초과하는 제목으로 생성 실패한다")
        void createNew_withTooLongTitle_fail() {
            String longTitle = "a".repeat(201);

            Validation<Seq<String>, MovieTitle> result = MovieTitle.createNew(longTitle, uniqueCondition);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("영화 제목은 200자를 초과할 수 없습니다.");
        }

        @Test
        @DisplayName("중복된 제목으로 생성 실패한다")
        void createNew_withDuplicateTitle_fail() {
            assertThatThrownBy(() -> MovieTitle.createNew("인터스텔라", duplicateCondition))
                    .isInstanceOf(MovieDomainException.class)
                    .hasMessageContaining("동일한 제목의 영화가 이미 존재합니다.");
        }

        @Test
        @DisplayName("정확히 200자의 제목으로 생성 성공한다")
        void createNew_withExactly200Characters_success() {
            String titleWith200Chars = "a".repeat(200);

            Validation<Seq<String>, MovieTitle> result = MovieTitle.createNew(titleWith200Chars, uniqueCondition);

            assertThat(result.isValid()).isTrue();
            assertThat(result.get().getTitle()).isEqualTo(titleWith200Chars);
        }
    }

    @Nested
    @DisplayName("createFrom 메서드 테스트")
    class CreateFromTest {

        private MovieTitle existingTitle;

        @BeforeEach
        void setUp() {
            existingTitle = MovieTitle.createNew("기존 영화 제목", uniqueCondition).get();
        }

        @Test
        @DisplayName("다른 유효한 제목으로 변경 성공한다")
        void createFrom_withDifferentValidTitle_success() {
            String newTitle = "새로운 영화 제목";

            Validation<Seq<String>, MovieTitle> result = MovieTitle.createFrom(
                    existingTitle,
                    newTitle,
                    uniqueCondition
            );

            assertThat(result.isValid()).isTrue();
            assertThat(result.get().getTitle()).isEqualTo(newTitle);
        }

        @Test
        @DisplayName("동일한 제목으로 변경하면 중복 조건과 무관하게 성공한다")
        void createFrom_withSameTitle_successWithoutDuplicateCheck() {
            String sameTitle = "기존 영화 제목";

            Validation<Seq<String>, MovieTitle> result = MovieTitle.createFrom(
                    existingTitle,
                    sameTitle,
                    duplicateCondition
            );

            assertThat(result.isValid()).isTrue();
            assertThat(result.get().getTitle()).isEqualTo(sameTitle);
        }

        @Test
        @DisplayName("null 제목으로 변경 실패한다")
        void createFrom_withNullTitle_fail() {
            Validation<Seq<String>, MovieTitle> result = MovieTitle.createFrom(
                    existingTitle,
                    null,
                    uniqueCondition
            );

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("영화 제목은 필수입니다.");
        }

        @Test
        @DisplayName("빈 문자열 제목으로 변경 실패한다")
        void createFrom_withEmptyTitle_fail() {
            Validation<Seq<String>, MovieTitle> result = MovieTitle.createFrom(
                    existingTitle,
                    "",
                    uniqueCondition
            );

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("영화 제목은 필수입니다.");
        }

        @Test
        @DisplayName("200자를 초과하는 제목으로 변경 실패한다")
        void createFrom_withTooLongTitle_fail() {
            String longTitle = "a".repeat(201);

            Validation<Seq<String>, MovieTitle> result = MovieTitle.createFrom(
                    existingTitle,
                    longTitle,
                    uniqueCondition
            );

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("영화 제목은 200자를 초과할 수 없습니다.");
        }

        @Test
        @DisplayName("중복된 제목으로 변경 실패한다")
        void createFrom_withDuplicateTitle_fail() {
            assertThatThrownBy(() -> MovieTitle.createFrom(existingTitle, "중복된 제목", duplicateCondition))
                    .isInstanceOf(MovieDomainException.class)
                    .hasMessageContaining("동일한 제목의 영화가 이미 존재합니다.");
        }

        @Test
        @DisplayName("정확히 200자의 제목으로 변경 성공한다")
        void createFrom_withExactly200Characters_success() {
            String titleWith200Chars = "b".repeat(200);

            Validation<Seq<String>, MovieTitle> result = MovieTitle.createFrom(
                    existingTitle,
                    titleWith200Chars,
                    uniqueCondition
            );

            assertThat(result.isValid()).isTrue();
            assertThat(result.get().getTitle()).isEqualTo(titleWith200Chars);
        }
    }
}
