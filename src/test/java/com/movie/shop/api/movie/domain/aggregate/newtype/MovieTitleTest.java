package com.movie.shop.api.movie.domain.aggregate.newtype;

import com.movie.shop.api.movie.domain.aggregate.MovieTitle;
import com.movie.shop.api.movie.domain.exceptions.MovieDomainException;
import com.movie.shop.api.movie.domain.policy.MovieTitlePolicy;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovieTitle 단위 테스트")
class MovieTitleTest {

    @Mock
    private MovieTitlePolicy mockValidator;

    @Nested
    @DisplayName("createNew 메서드 테스트")
    class CreateNewTest {

        @Test
        @DisplayName("유효한 제목으로 생성 성공한다")
        void createNew_withValidTitle_success() {
            // given
            String validTitle = "인터스텔라";

            // when
            Validation<Seq<String>, MovieTitle> result = MovieTitle.createNew(validTitle, mockValidator);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get().getTitle()).isEqualTo(validTitle);
        }

        @Test
        @DisplayName("null 제목으로 생성 실패한다")
        void createNew_withNullTitle_fail() {
            // when
            Validation<Seq<String>, MovieTitle> result = MovieTitle.createNew(null, mockValidator);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("영화 제목은 필수입니다.");
        }

        @Test
        @DisplayName("빈 문자열 제목으로 생성 실패한다")
        void createNew_withEmptyTitle_fail() {
            // when
            Validation<Seq<String>, MovieTitle> result = MovieTitle.createNew("", mockValidator);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("영화 제목은 필수입니다.");
        }

        @Test
        @DisplayName("200자를 초과하는 제목으로 생성 실패한다")
        void createNew_withTooLongTitle_fail() {
            // given
            String longTitle = "a".repeat(201);

            // when
            Validation<Seq<String>, MovieTitle> result = MovieTitle.createNew(longTitle, mockValidator);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("영화 제목은 200자를 초과할 수 없습니다.");
        }

        @Test
        @DisplayName("중복된 제목으로 생성 실패한다")
        void createNew_withDuplicateTitle_fail() {
            doThrow(new MovieDomainException("동일한 제목의 영화가 이미 존재합니다."))
                    .when(mockValidator)
                    .validateNotDuplicate();

            assertThatThrownBy(() -> MovieTitle.createNew("인터스텔라", mockValidator))
                    .isInstanceOf(MovieDomainException.class)
                    .hasMessageContaining("동일한 제목의 영화가 이미 존재합니다.");
        }

        @Test
        @DisplayName("정확히 200자의 제목으로 생성 성공한다")
        void createNew_withExactly200Characters_success() {
            // given
            String titleWith200Chars = "a".repeat(200);

            // when
            Validation<Seq<String>, MovieTitle> result = MovieTitle.createNew(titleWith200Chars, mockValidator);

            // then
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
            existingTitle = MovieTitle.createNew("기존 영화 제목", mockValidator).get();
        }

        @Test
        @DisplayName("다른 유효한 제목으로 변경 성공한다")
        void createFrom_withDifferentValidTitle_success() {
            // given
            String newTitle = "새로운 영화 제목";

            // when
            Validation<Seq<String>, MovieTitle> result = MovieTitle.createFrom(existingTitle, newTitle, mockValidator);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get().getTitle()).isEqualTo(newTitle);
        }

        @Test
        @DisplayName("동일한 제목으로 변경하면 중복 검증 스킵하고 성공한다")
        void createFrom_withSameTitle_successWithoutDuplicateCheck() {
            String sameTitle = "기존 영화 제목";

            // when
            Validation<Seq<String>, MovieTitle> result = MovieTitle.createFrom(existingTitle, sameTitle, mockValidator);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get().getTitle()).isEqualTo(sameTitle);
        }

        @Test
        @DisplayName("null 제목으로 변경 실패한다")
        void createFrom_withNullTitle_fail() {
            // when
            Validation<Seq<String>, MovieTitle> result = MovieTitle.createFrom(existingTitle, null, mockValidator);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("영화 제목은 필수입니다.");
        }

        @Test
        @DisplayName("빈 문자열 제목으로 변경 실패한다")
        void createFrom_withEmptyTitle_fail() {
            // when
            Validation<Seq<String>, MovieTitle> result = MovieTitle.createFrom(existingTitle, "", mockValidator);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("영화 제목은 필수입니다.");
        }

        @Test
        @DisplayName("200자를 초과하는 제목으로 변경 실패한다")
        void createFrom_withTooLongTitle_fail() {
            // given
            String longTitle = "a".repeat(201);

            // when
            Validation<Seq<String>, MovieTitle> result = MovieTitle.createFrom(existingTitle, longTitle, mockValidator);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("영화 제목은 200자를 초과할 수 없습니다.");
        }

        @Test
        @DisplayName("중복된 제목으로 변경 실패한다")
        void createFrom_withDuplicateTitle_fail() {
            doThrow(new MovieDomainException("동일한 제목의 영화가 이미 존재합니다."))
                    .when(mockValidator)
                    .validateNotDuplicate();

            assertThatThrownBy(() -> MovieTitle.createFrom(existingTitle, "중복된 제목", mockValidator))
                    .isInstanceOf(MovieDomainException.class)
                    .hasMessageContaining("동일한 제목의 영화가 이미 존재합니다.");
        }

        @Test
        @DisplayName("정확히 200자의 제목으로 변경 성공한다")
        void createFrom_withExactly200Characters_success() {
            // given
            String titleWith200Chars = "b".repeat(200);

            // when
            Validation<Seq<String>, MovieTitle> result = MovieTitle.createFrom(existingTitle, titleWith200Chars, mockValidator);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get().getTitle()).isEqualTo(titleWith200Chars);
        }
    }
}
