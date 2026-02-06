package com.movie.shop.api.theater.domain.aggregate;

import com.movie.shop.api.theater.domain.aggregate.validator.TheaterNameDuplicateValidator;
import com.movie.shop.api.theater.domain.exceptions.TheaterDomainException;
import com.movie.shop.api.theater.domain.policy.TheaterScreeningProtectionPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Theater 단위 테스트")
class TheaterTest {

    @Mock
    private TheaterNameDuplicateValidator validator;

    @Mock
    private TheaterScreeningProtectionPolicy theaterScreeningProtectionPolicy;

    private String validName;
    private int validFloor;
    private TheaterType validType;
    private List<String> validSeats;
    private int validRowCount;
    private int validColumnCount;

    @BeforeEach
    void setUp() {
        validName = "1관";
        validFloor = 1;
        validType = TheaterType.Standard;
        validSeats = List.of("A1", "A2", "A3", "B1", "B2", "B3");
        validRowCount = 2;
        validColumnCount = 3;

        when(validator.validateNotDuplicate(validName)).thenReturn(true);
    }

    @Test
    @DisplayName("유효한 데이터로 등록 성공한다")
    void register_withValidData_success() {
        // when
        Theater theater = Theater.Register(
                validator,
                validName,
                validFloor,
                validType,
                validSeats,
                validRowCount,
                validColumnCount
        );

        // then
        assertThat(theater.getName().getName()).isEqualTo(validName);
        assertThat(theater.getFloor()).isEqualTo(validFloor);
        assertThat(theater.getTheaterType()).isEqualTo(validType);
        assertThat(theater.getSeats().getSeats()).containsExactlyElementsOf(validSeats);
        assertThat(theater.isActive()).isTrue();
    }

    @Test
    @DisplayName("층수가 -10 미만이면 등록 실패한다")
    void register_withFloorTooSmall_fail() {
        assertThatThrownBy(() -> Theater.Register(
                validator,
                validName,
                -11,
                validType,
                validSeats,
                validRowCount,
                validColumnCount
        ))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("층수는 -10에서 100 사이여야 합니다.");
    }

    @Test
    @DisplayName("층수가 100 초과면 등록 실패한다")
    void register_withFloorTooLarge_fail() {
        assertThatThrownBy(() -> Theater.Register(
                validator,
                validName,
                101,
                validType,
                validSeats,
                validRowCount,
                validColumnCount
        ))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("층수는 -10에서 100 사이여야 합니다.");
    }

    @Test
    @DisplayName("활성 상태이면 상영 가능 여부가 true를 반환한다")
    void canHostScreening_whenActive_returnsTrue() {
        Theater theater = Theater.Register(
                validator,
                validName,
                validFloor,
                validType,
                validSeats,
                validRowCount,
                validColumnCount
        );

        assertThat(theater.canHostScreening()).isTrue();
    }

    @Test
    @DisplayName("비활성 상태이면 상영 가능 여부가 false를 반환한다")
    void canHostScreening_whenInactive_returnsFalse() {
        Theater theater = Theater.Register(
                validator,
                validName,
                validFloor,
                validType,
                validSeats,
                validRowCount,
                validColumnCount
        );
        theater.changeActive(TheaterActiveChange.DEACTIVATE, theaterScreeningProtectionPolicy);

        assertThat(theater.canHostScreening()).isFalse();
    }

    @Test
    @DisplayName("DEACTIVATE 요청 시 정책에서 차단되면 예외가 발생한다")
    void changeActive_whenDeactivateBlocked_throwsException() {
        Theater theater = Theater.Register(
                validator,
                validName,
                validFloor,
                validType,
                validSeats,
                validRowCount,
                validColumnCount
        );
        doThrow(new TheaterDomainException("차단"))
                .when(theaterScreeningProtectionPolicy)
                .validateCanChangeActive(theater, TheaterActiveChange.DEACTIVATE);

        assertThatThrownBy(() -> theater.changeActive(TheaterActiveChange.DEACTIVATE, theaterScreeningProtectionPolicy))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("차단");
    }

    @Test
    @DisplayName("DEACTIVATE 요청 시 정책 검증을 통과하면 비활성 상태로 변경된다")
    void changeActive_whenDeactivateAllowed_becomesInactive() {
        Theater theater = Theater.Register(
                validator,
                validName,
                validFloor,
                validType,
                validSeats,
                validRowCount,
                validColumnCount
        );

        theater.changeActive(TheaterActiveChange.DEACTIVATE, theaterScreeningProtectionPolicy);

        assertThat(theater.isActive()).isFalse();
    }

    @Test
    @DisplayName("ACTIVATE 요청하면 정책 검증 없이 활성 상태로 변경된다")
    void changeActive_whenActivate_skipsPolicyValidation() throws Exception {
        Theater theater = Theater.Register(
                validator,
                validName,
                validFloor,
                validType,
                validSeats,
                validRowCount,
                validColumnCount
        );
        setActive(theater, false);

        theater.changeActive(TheaterActiveChange.ACTIVATE, theaterScreeningProtectionPolicy);

        assertThat(theater.isActive()).isTrue();
        verifyNoInteractions(theaterScreeningProtectionPolicy);
    }

    @Test
    @DisplayName("정책이 null이면 상태 변경 시 예외가 발생한다")
    void changeActive_whenPolicyNull_throwsException() {
        Theater theater = Theater.Register(
                validator,
                validName,
                validFloor,
                validType,
                validSeats,
                validRowCount,
                validColumnCount
        );

        assertThatThrownBy(() -> theater.changeActive(TheaterActiveChange.DEACTIVATE, null))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("정책은 필수");
    }

    private void setActive(Theater theater, boolean active) throws Exception {
        Field activeField = Theater.class.getDeclaredField("active");
        activeField.setAccessible(true);
        activeField.set(theater, active);
    }

}
