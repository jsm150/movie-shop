package com.movie.shop.api.theater.domain.aggregate.newtype;

import com.movie.shop.api.theater.domain.aggregate.validator.TheaterNameDuplicateValidator;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TheaterName 단위 테스트")
class TheaterNameTest {

    @Mock
    private TheaterNameDuplicateValidator mockValidator;

    @Nested
    @DisplayName("createNew 메서드 테스트")
    class CreateNewTest {

        @Test
        @DisplayName("유효한 상영관 이름으로 생성 성공")
        void createNew_withValidName_success() {
            // given
            String validName = "1관";
            when(mockValidator.validateNotDuplicate(validName)).thenReturn(true);

            // when
            Validation<Seq<String>, TheaterName> result = TheaterName.createNew(validName, mockValidator);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get().getName()).isEqualTo(validName);
        }

        @Test
        @DisplayName("null 이름으로 생성 실패")
        void createNew_withNullName_fail() {
            // when
            Validation<Seq<String>, TheaterName> result = TheaterName.createNew(null, mockValidator);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("상영관 이름은 필수입니다.");
        }

        @Test
        @DisplayName("빈 문자열 이름으로 생성 실패")
        void createNew_withEmptyName_fail() {
            // when
            Validation<Seq<String>, TheaterName> result = TheaterName.createNew("", mockValidator);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("상영관 이름은 필수입니다.");
        }

        @Test
        @DisplayName("50자를 초과하는 이름으로 생성 실패")
        void createNew_withTooLongName_fail() {
            // given
            String longName = "a".repeat(51);

            // when
            Validation<Seq<String>, TheaterName> result = TheaterName.createNew(longName, mockValidator);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("상영관 이름은 50자를 초과할 수 없습니다.");
        }

        @Test
        @DisplayName("중복된 이름으로 생성 실패")
        void createNew_withDuplicateName_fail() {
            // given
            String duplicateName = "1관";
            when(mockValidator.validateNotDuplicate(duplicateName)).thenReturn(false);

            // when
            Validation<Seq<String>, TheaterName> result = TheaterName.createNew(duplicateName, mockValidator);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("'" + duplicateName + "' 이름의 상영관이 이미 존재합니다.");
        }

        @Test
        @DisplayName("정확히 50자의 이름으로 생성 성공")
        void createNew_withExactly50Characters_success() {
            // given
            String nameWith50Chars = "a".repeat(50);
            when(mockValidator.validateNotDuplicate(nameWith50Chars)).thenReturn(true);

            // when
            Validation<Seq<String>, TheaterName> result = TheaterName.createNew(nameWith50Chars, mockValidator);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get().getName()).isEqualTo(nameWith50Chars);
        }
    }

    @Nested
    @DisplayName("createFrom 메서드 테스트")
    class CreateFromTest {

        private TheaterName existingName;

        @BeforeEach
        void setUp() {
            when(mockValidator.validateNotDuplicate("기존 상영관")).thenReturn(true);
            existingName = TheaterName.createNew("기존 상영관", mockValidator).get();
        }

        @Test
        @DisplayName("다른 유효한 이름으로 변경 성공")
        void createFrom_withDifferentValidName_success() {
            // given
            String newName = "새 상영관";
            when(mockValidator.validateNotDuplicate(newName)).thenReturn(true);

            // when
            Validation<Seq<String>, TheaterName> result = TheaterName.createFrom(existingName, newName, mockValidator);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get().getName()).isEqualTo(newName);
        }

        @Test
        @DisplayName("동일한 이름으로 변경 시 중복 검증 스킵하고 성공")
        void createFrom_withSameName_successWithoutDuplicateCheck() {
            // given
            String sameName = "기존 상영관";
            lenient().when(mockValidator.validateNotDuplicate(existingName.getName())).thenReturn(false);

            // when
            Validation<Seq<String>, TheaterName> result = TheaterName.createFrom(existingName, sameName, mockValidator);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get().getName()).isEqualTo(sameName);
        }

        @Test
        @DisplayName("null 이름으로 변경 실패")
        void createFrom_withNullName_fail() {
            // when
            Validation<Seq<String>, TheaterName> result = TheaterName.createFrom(existingName, null, mockValidator);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("상영관 이름은 필수입니다.");
        }

        @Test
        @DisplayName("빈 문자열 이름으로 변경 실패")
        void createFrom_withEmptyName_fail() {
            // when
            Validation<Seq<String>, TheaterName> result = TheaterName.createFrom(existingName, "", mockValidator);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("상영관 이름은 필수입니다.");
        }

        @Test
        @DisplayName("50자를 초과하는 이름으로 변경 실패")
        void createFrom_withTooLongName_fail() {
            // given
            String longName = "a".repeat(51);

            // when
            Validation<Seq<String>, TheaterName> result = TheaterName.createFrom(existingName, longName, mockValidator);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("상영관 이름은 50자를 초과할 수 없습니다.");
        }

        @Test
        @DisplayName("중복된 이름으로 변경 실패")
        void createFrom_withDuplicateName_fail() {
            // given
            String duplicateName = "중복 상영관";
            when(mockValidator.validateNotDuplicate(duplicateName)).thenReturn(false);

            // when
            Validation<Seq<String>, TheaterName> result = TheaterName.createFrom(existingName, duplicateName, mockValidator);

            // then
            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("'" + duplicateName + "' 이름의 상영관이 이미 존재합니다.");
        }

        @Test
        @DisplayName("정확히 50자의 이름으로 변경 성공")
        void createFrom_withExactly50Characters_success() {
            // given
            String nameWith50Chars = "b".repeat(50);
            when(mockValidator.validateNotDuplicate(nameWith50Chars)).thenReturn(true);

            // when
            Validation<Seq<String>, TheaterName> result = TheaterName.createFrom(existingName, nameWith50Chars, mockValidator);

            // then
            assertThat(result.isValid()).isTrue();
            assertThat(result.get().getName()).isEqualTo(nameWith50Chars);
        }
    }
}
