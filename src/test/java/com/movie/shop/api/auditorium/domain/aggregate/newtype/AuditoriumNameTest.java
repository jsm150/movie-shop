package com.movie.shop.api.auditorium.domain.aggregate.newtype;

import com.movie.shop.api.auditorium.domain.aggregate.AuditoriumName;
import com.movie.shop.api.auditorium.domain.policy.AuditoriumNameDuplicatePolicy;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditoriumName 단위 테스트")
class AuditoriumNameTest {

    @Mock
    private AuditoriumNameDuplicatePolicy mockValidator;

    @Nested
    @DisplayName("createNew 메서드 테스트")
    class CreateNewTest {

        @Test
        @DisplayName("유효한 상영관 이름으로 생성 성공한다")
        void createNew_withValidName_success() {
            long theaterId = 1L;
            String validName = "1관";
            when(mockValidator.validateNotDuplicate(theaterId, validName)).thenReturn(true);

            Validation<Seq<String>, AuditoriumName> result = AuditoriumName.createNew(theaterId, validName, mockValidator);

            assertThat(result.isValid()).isTrue();
            assertThat(result.get().getName()).isEqualTo(validName);
            verify(mockValidator).validateNotDuplicate(theaterId, validName);
        }

        @Test
        @DisplayName("null 이름으로 생성 실패한다")
        void createNew_withNullName_fail() {
            Validation<Seq<String>, AuditoriumName> result = AuditoriumName.createNew(1L, null, mockValidator);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("상영관 이름은 필수입니다.");
        }

        @Test
        @DisplayName("빈 문자열 이름으로 생성 실패한다")
        void createNew_withEmptyName_fail() {
            Validation<Seq<String>, AuditoriumName> result = AuditoriumName.createNew(1L, "", mockValidator);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("상영관 이름은 필수입니다.");
        }

        @Test
        @DisplayName("50자를 초과하는 이름으로 생성 실패한다")
        void createNew_withTooLongName_fail() {
            String longName = "a".repeat(51);

            Validation<Seq<String>, AuditoriumName> result = AuditoriumName.createNew(1L, longName, mockValidator);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("상영관 이름은 50자를 초과할 수 없습니다.");
        }

        @Test
        @DisplayName("동일 theaterId에서 중복 이름이면 생성 실패한다")
        void createNew_withDuplicateNameInSameTheater_fail() {
            long theaterId = 1L;
            String duplicateName = "1관";
            when(mockValidator.validateNotDuplicate(theaterId, duplicateName)).thenReturn(false);

            Validation<Seq<String>, AuditoriumName> result = AuditoriumName.createNew(theaterId, duplicateName, mockValidator);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("'" + duplicateName + "' 이름의 상영관이 해당 영화관에 이미 존재합니다.");
            verify(mockValidator).validateNotDuplicate(theaterId, duplicateName);
        }

        @Test
        @DisplayName("다른 theaterId면 같은 이름도 생성 가능하다")
        void createNew_withSameNameInDifferentTheater_success() {
            long theaterId = 2L;
            String sameName = "1관";
            when(mockValidator.validateNotDuplicate(theaterId, sameName)).thenReturn(true);

            Validation<Seq<String>, AuditoriumName> result = AuditoriumName.createNew(theaterId, sameName, mockValidator);

            assertThat(result.isValid()).isTrue();
            assertThat(result.get().getName()).isEqualTo(sameName);
            verify(mockValidator).validateNotDuplicate(theaterId, sameName);
        }
    }

    @Nested
    @DisplayName("createFrom 메서드 테스트")
    class CreateFromTest {

        private AuditoriumName existingName;
        private long theaterId;

        @BeforeEach
        void setUp() {
            theaterId = 1L;
            when(mockValidator.validateNotDuplicate(theaterId, "기존 상영관")).thenReturn(true);
            existingName = AuditoriumName.createNew(theaterId, "기존 상영관", mockValidator).get();
        }

        @Test
        @DisplayName("다른 유효한 이름으로 변경 성공한다")
        void createFrom_withDifferentValidName_success() {
            String newName = "새 상영관";
            when(mockValidator.validateNotDuplicate(theaterId, newName)).thenReturn(true);

            Validation<Seq<String>, AuditoriumName> result = AuditoriumName.createFrom(theaterId, existingName, newName, mockValidator);

            assertThat(result.isValid()).isTrue();
            assertThat(result.get().getName()).isEqualTo(newName);
        }

        @Test
        @DisplayName("동일한 이름으로 변경하면 중복 검증을 스킵하고 성공한다")
        void createFrom_withSameName_successWithoutDuplicateCheck() {
            String sameName = "기존 상영관";
            lenient().when(mockValidator.validateNotDuplicate(theaterId, existingName.getName())).thenReturn(false);

            Validation<Seq<String>, AuditoriumName> result = AuditoriumName.createFrom(theaterId, existingName, sameName, mockValidator);

            assertThat(result.isValid()).isTrue();
            assertThat(result.get().getName()).isEqualTo(sameName);
        }

        @Test
        @DisplayName("null 이름으로 변경 실패한다")
        void createFrom_withNullName_fail() {
            Validation<Seq<String>, AuditoriumName> result = AuditoriumName.createFrom(theaterId, existingName, null, mockValidator);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("상영관 이름은 필수입니다.");
        }

        @Test
        @DisplayName("빈 문자열 이름으로 변경 실패한다")
        void createFrom_withEmptyName_fail() {
            Validation<Seq<String>, AuditoriumName> result = AuditoriumName.createFrom(theaterId, existingName, "", mockValidator);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("상영관 이름은 필수입니다.");
        }

        @Test
        @DisplayName("50자를 초과하는 이름으로 변경 실패한다")
        void createFrom_withTooLongName_fail() {
            String longName = "a".repeat(51);

            Validation<Seq<String>, AuditoriumName> result = AuditoriumName.createFrom(theaterId, existingName, longName, mockValidator);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("상영관 이름은 50자를 초과할 수 없습니다.");
        }

        @Test
        @DisplayName("동일 theaterId에서 중복된 이름으로 변경 실패한다")
        void createFrom_withDuplicateName_fail() {
            String duplicateName = "중복 상영관";
            when(mockValidator.validateNotDuplicate(theaterId, duplicateName)).thenReturn(false);

            Validation<Seq<String>, AuditoriumName> result = AuditoriumName.createFrom(theaterId, existingName, duplicateName, mockValidator);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getError()).contains("'" + duplicateName + "' 이름의 상영관이 해당 영화관에 이미 존재합니다.");
        }
    }
}
