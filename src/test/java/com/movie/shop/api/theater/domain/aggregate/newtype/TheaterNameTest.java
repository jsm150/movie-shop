package com.movie.shop.api.theater.domain.aggregate.newtype;

import com.movie.shop.api.theater.domain.aggregate.TheaterName;
import com.movie.shop.api.theater.domain.condition.TheaterNameUniquenessCondition;
import com.movie.shop.api.theater.domain.exceptions.TheaterDomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TheaterName 단위 테스트")
class TheaterNameTest {

    private TheaterNameUniquenessCondition uniqueNameCondition;
    private TheaterNameUniquenessCondition duplicateNameCondition;

    @BeforeEach
    void setUp() {
        uniqueNameCondition = new TheaterNameUniquenessCondition(true);
        duplicateNameCondition = new TheaterNameUniquenessCondition(false);
    }

    @Nested
    @DisplayName("createNew 메서드 테스트")
    class CreateNewTest {

        @Test
        @DisplayName("유효한 영화관 이름으로 생성 성공한다")
        void createNew_withValidName_success() {
            String validName = "강남점";

            TheaterName result = TheaterName.createNew(validName, uniqueNameCondition);

            assertThat(result.getName()).isEqualTo(validName);
        }

        @Test
        @DisplayName("null 이름으로 생성 실패한다")
        void createNew_withNullName_fail() {
            assertThatThrownBy(() -> TheaterName.createNew(null, uniqueNameCondition))
                    .isInstanceOf(TheaterDomainException.class)
                    .hasMessageContaining("영화관 이름은 필수입니다.");
        }

        @Test
        @DisplayName("빈 문자열 이름으로 생성 실패한다")
        void createNew_withEmptyName_fail() {
            assertThatThrownBy(() -> TheaterName.createNew("", uniqueNameCondition))
                    .isInstanceOf(TheaterDomainException.class)
                    .hasMessageContaining("영화관 이름은 필수입니다.");
        }

        @Test
        @DisplayName("공백 이름으로 생성 실패한다")
        void createNew_withBlankName_fail() {
            assertThatThrownBy(() -> TheaterName.createNew(" ", uniqueNameCondition))
                    .isInstanceOf(TheaterDomainException.class)
                    .hasMessageContaining("영화관 이름은 필수입니다.");
        }

        @Test
        @DisplayName("50자를 초과하는 이름으로 생성 실패한다")
        void createNew_withTooLongName_fail() {
            String longName = "a".repeat(51);

            assertThatThrownBy(() -> TheaterName.createNew(longName, uniqueNameCondition))
                    .isInstanceOf(TheaterDomainException.class)
                    .hasMessageContaining("영화관 이름은 50자를 초과할 수 없습니다.");
        }

        @Test
        @DisplayName("여러 어노테이션 검증 오류를 한 번에 반환한다")
        void createNew_withMultipleValidationErrors_collectsErrors() {
            String invalidName = " ".repeat(51);

            assertThatThrownBy(() -> TheaterName.createNew(invalidName, uniqueNameCondition))
                    .isInstanceOf(TheaterDomainException.class)
                    .satisfies(exception -> assertThat(
                            ((TheaterDomainException) exception).getErrors()
                    ).contains(
                            "영화관 이름은 필수입니다.",
                            "영화관 이름은 50자를 초과할 수 없습니다."
                    ));
        }

        @Test
        @DisplayName("중복 조건이 없으면 생성 실패한다")
        void createNew_withNullCondition_fail() {
            assertThatThrownBy(() -> TheaterName.createNew("강남점", null))
                    .isInstanceOf(TheaterDomainException.class)
                    .hasMessageContaining("영화관 이름 중복 조건은 필수입니다.");
        }

        @Test
        @DisplayName("중복된 이름으로 생성 실패한다")
        void createNew_withDuplicateName_fail() {
            assertThatThrownBy(() -> TheaterName.createNew("강남점", duplicateNameCondition))
                    .isInstanceOf(TheaterDomainException.class)
                    .hasMessageContaining("동일한 이름의 영화관이 이미 존재합니다.");
        }
    }

    @Nested
    @DisplayName("createFrom 메서드 테스트")
    class CreateFromTest {

        private TheaterName existingName;

        @BeforeEach
        void setUp() {
            existingName = TheaterName.createNew("기존영화관", uniqueNameCondition);
        }

        @Test
        @DisplayName("다른 유효한 이름으로 변경 성공한다")
        void createFrom_withDifferentValidName_success() {
            String newName = "새영화관";

            TheaterName result = TheaterName.createFrom(existingName, newName, uniqueNameCondition);

            assertThat(result.getName()).isEqualTo(newName);
        }

        @Test
        @DisplayName("동일한 이름으로 변경하면 중복 검증 스킵하고 성공한다")
        void createFrom_withSameName_successWithoutDuplicateCheck() {
            String sameName = "기존영화관";

            TheaterName result = TheaterName.createFrom(existingName, sameName, duplicateNameCondition);

            assertThat(result.getName()).isEqualTo(sameName);
        }

        @Test
        @DisplayName("중복된 이름으로 변경 실패한다")
        void createFrom_withDuplicateName_fail() {
            assertThatThrownBy(() -> TheaterName.createFrom(existingName, "중복영화관", duplicateNameCondition))
                    .isInstanceOf(TheaterDomainException.class)
                    .hasMessageContaining("동일한 이름의 영화관이 이미 존재합니다.");
        }

        @Test
        @DisplayName("중복 조건이 없으면 수정 실패한다")
        void createFrom_withNullCondition_fail() {
            assertThatThrownBy(() -> TheaterName.createFrom(existingName, "새영화관", null))
                    .isInstanceOf(TheaterDomainException.class)
                    .hasMessageContaining("영화관 이름 중복 조건은 필수입니다.");
        }
    }
}
