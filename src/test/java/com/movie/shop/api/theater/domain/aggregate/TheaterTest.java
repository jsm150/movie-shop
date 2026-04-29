package com.movie.shop.api.theater.domain.aggregate;

import com.movie.shop.api.theater.domain.condition.TheaterAuditoriumPresence;
import com.movie.shop.api.theater.domain.condition.TheaterNameUniquenessCondition;
import com.movie.shop.api.theater.domain.exceptions.TheaterDomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Theater 단위 테스트")
class TheaterTest {

    private TheaterNameUniquenessCondition uniqueNameCondition;
    private TheaterNameUniquenessCondition duplicateNameCondition;
    private TheaterAuditoriumPresence linkedAuditoriumPresence;
    private TheaterAuditoriumPresence emptyAuditoriumPresence;
    private String validName;

    @BeforeEach
    void setUp() {
        uniqueNameCondition = new TheaterNameUniquenessCondition(true);
        duplicateNameCondition = new TheaterNameUniquenessCondition(false);
        linkedAuditoriumPresence = new TheaterAuditoriumPresence(true);
        emptyAuditoriumPresence = new TheaterAuditoriumPresence(false);
        validName = "강남점";
    }

    @Test
    @DisplayName("유효한 데이터로 등록 성공한다")
    void register_withValidData_success() {
        Theater theater = Theater.register(validName, uniqueNameCondition);

        assertThat(theater.getName().getName()).isEqualTo(validName);
        assertThat(theater.isActive()).isTrue();
    }

    @Test
    @DisplayName("빈 이름으로 등록 실패한다")
    void register_withBlankName_fail() {
        assertThatThrownBy(() -> Theater.register("", uniqueNameCondition))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("영화관 이름은 필수입니다.");
    }

    @Test
    @DisplayName("중복된 이름으로 등록하면 실패한다")
    void register_withDuplicateName_fail() {
        assertThatThrownBy(() -> Theater.register(validName, duplicateNameCondition))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("이미 존재합니다.");
    }

    @Test
    @DisplayName("등록된 영화관 이름을 수정한다")
    void updateName_withValidName_success() {
        Theater theater = Theater.register(validName, uniqueNameCondition);

        theater.updateName("홍대점", uniqueNameCondition);

        assertThat(theater.getName().getName()).isEqualTo("홍대점");
    }

    @Test
    @DisplayName("동일한 이름으로 수정하면 중복 조건과 무관하게 성공한다")
    void updateName_withSameName_successWithoutDuplicateCheck() {
        Theater theater = Theater.register(validName, uniqueNameCondition);

        theater.updateName(validName, duplicateNameCondition);

        assertThat(theater.getName().getName()).isEqualTo(validName);
    }

    @Test
    @DisplayName("중복된 이름으로 수정하면 실패한다")
    void updateName_withDuplicateName_fail() {
        Theater theater = Theater.register(validName, uniqueNameCondition);

        assertThatThrownBy(() -> theater.updateName("홍대점", duplicateNameCondition))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("이미 존재합니다.");
    }

    @Test
    @DisplayName("연결된 상영관이 있는 활성 영화관은 비활성화할 수 없다")
    void changeActive_whenDeactivateBlocked_throwsException() {
        Theater theater = Theater.register(validName, uniqueNameCondition);

        assertThatThrownBy(() -> theater.changeActive(TheaterActiveChange.DEACTIVATE, linkedAuditoriumPresence))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("비활성화할 수 없습니다.");
    }

    @Test
    @DisplayName("연결된 상영관이 없으면 비활성 상태로 변경된다")
    void changeActive_whenDeactivateAllowed_becomesInactive() {
        Theater theater = Theater.register(validName, uniqueNameCondition);

        theater.changeActive(TheaterActiveChange.DEACTIVATE, emptyAuditoriumPresence);

        assertThat(theater.isActive()).isFalse();
    }

    @Test
    @DisplayName("이미 비활성 상태면 연결된 상영관이 있어도 비활성화 요청이 통과한다")
    void changeActive_whenAlreadyInactive_doesNotThrow() throws Exception {
        Theater theater = Theater.register(validName, uniqueNameCondition);
        setActive(theater, false);

        assertThatCode(() -> theater.changeActive(TheaterActiveChange.DEACTIVATE, linkedAuditoriumPresence))
                .doesNotThrowAnyException();
        assertThat(theater.isActive()).isFalse();
    }

    @Test
    @DisplayName("ACTIVATE 요청하면 상영관 보유 여부와 무관하게 활성 상태로 변경된다")
    void changeActive_whenActivate_ignoresAuditoriumPresence() throws Exception {
        Theater theater = Theater.register(validName, uniqueNameCondition);
        setActive(theater, false);

        theater.changeActive(TheaterActiveChange.ACTIVATE, linkedAuditoriumPresence);

        assertThat(theater.isActive()).isTrue();
    }

    @Test
    @DisplayName("상태 변경 요청이 null이면 예외가 발생한다")
    void changeActive_whenStateNull_throwsException() {
        Theater theater = Theater.register(validName, uniqueNameCondition);

        assertThatThrownBy(() -> theater.changeActive(null, emptyAuditoriumPresence))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("활성 상태는 필수");
    }

    @Test
    @DisplayName("연결된 상영관이 있는 영화관은 삭제할 수 없다")
    void validateCanDelete_whenLinkedAuditoriumExists_throwsException() {
        Theater theater = Theater.register(validName, uniqueNameCondition);

        assertThatThrownBy(() -> theater.validateCanDelete(linkedAuditoriumPresence))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("삭제할 수 없습니다.");
    }

    @Test
    @DisplayName("연결된 상영관이 없으면 삭제 검증에 성공한다")
    void validateCanDelete_whenNoLinkedAuditorium_doesNotThrow() {
        Theater theater = Theater.register(validName, uniqueNameCondition);

        assertThatCode(() -> theater.validateCanDelete(emptyAuditoriumPresence))
                .doesNotThrowAnyException();
    }

    private void setActive(Theater theater, boolean active) throws Exception {
        Field activeField = Theater.class.getDeclaredField("active");
        activeField.setAccessible(true);
        activeField.set(theater, active);
    }
}
