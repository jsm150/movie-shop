package com.movie.shop.api.theater.domain.aggregate;

import com.movie.shop.api.theater.domain.aggregate.validator.TheaterNameDuplicateValidator;
import com.movie.shop.api.theater.domain.exceptions.TheaterDomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Theater 단위 테스트")
class TheaterTest {

    @Mock
    private TheaterNameDuplicateValidator validator;

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
    @DisplayName("유효한 데이터로 등록 성공")
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
    @DisplayName("층수가 -10 미만이면 등록 실패")
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
    @DisplayName("층수가 100 초과면 등록 실패")
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
    @DisplayName("canHostScreening: active=true 이면 true")
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
    @DisplayName("canHostScreening: active=false 이면 false")
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
        theater.deactivate();

        assertThat(theater.canHostScreening()).isFalse();
    }

}
