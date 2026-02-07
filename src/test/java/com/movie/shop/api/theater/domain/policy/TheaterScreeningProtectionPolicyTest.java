package com.movie.shop.api.theater.domain.policy;

import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.aggregate.TheaterActiveChange;
import com.movie.shop.api.theater.domain.aggregate.TheaterType;
import com.movie.shop.api.theater.domain.exceptions.TheaterDomainException;
import com.movie.shop.api.theater.domain.port.CheckTheaterScreeningLinkPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TheaterScreeningProtectionPolicyTest {

    @Mock
    private CheckTheaterScreeningLinkPort checkTheaterScreeningLinkPort;

    @Mock
    private TheaterNameDuplicateValidator theaterNameDuplicateValidator;

    private TheaterScreeningProtectionPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new TheaterScreeningProtectionPolicy(checkTheaterScreeningLinkPort);
    }

    @Test
    @DisplayName("비활성화 차단 상영이 존재하면 비활성화 검증 시 예외가 발생한다")
    void validateCanChangeActive_whenDeactivateAndBlockingScreeningExists_throwsException() throws Exception {
        Theater theater = createTheater(1L, true);
        when(checkTheaterScreeningLinkPort.existsBlockingScreeningByTheaterId(1L)).thenReturn(true);

        assertThatThrownBy(() -> policy.validateCanChangeActive(theater, TheaterActiveChange.DEACTIVATE))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("비활성화할 수 없습니다.");
    }

    @Test
    @DisplayName("비활성화 차단 상영이 없으면 비활성화 검증에 성공한다")
    void validateCanChangeActive_whenDeactivateAndNoBlockingScreening_doesNotThrow() throws Exception {
        Theater theater = createTheater(1L, true);
        when(checkTheaterScreeningLinkPort.existsBlockingScreeningByTheaterId(1L)).thenReturn(false);

        assertThatCode(() -> policy.validateCanChangeActive(theater, TheaterActiveChange.DEACTIVATE))
                .doesNotThrowAnyException();

        verify(checkTheaterScreeningLinkPort).existsBlockingScreeningByTheaterId(1L);
    }

    @Test
    @DisplayName("이미 비활성 상태에서 비활성화를 요청하면 상영 연결 확인 포트를 호출하지 않는다")
    void validateCanChangeActive_whenAlreadyInactive_doesNotCallPort() throws Exception {
        Theater theater = createTheater(1L, false);

        assertThatCode(() -> policy.validateCanChangeActive(theater, TheaterActiveChange.DEACTIVATE))
                .doesNotThrowAnyException();

        verifyNoInteractions(checkTheaterScreeningLinkPort);
    }

    @Test
    @DisplayName("삭제 차단 상영이 존재하면 삭제 검증 시 예외가 발생한다")
    void validateCanDelete_whenBlockingScreeningExists_throwsException() throws Exception {
        Theater theater = createTheater(1L, true);
        when(checkTheaterScreeningLinkPort.existsBlockingScreeningByTheaterId(1L)).thenReturn(true);

        assertThatThrownBy(() -> policy.validateCanDelete(theater))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("삭제할 수 없습니다.");
    }

    @Test
    @DisplayName("삭제 차단 상영이 없으면 삭제 검증에 성공한다")
    void validateCanDelete_whenNoBlockingScreening_doesNotThrow() throws Exception {
        Theater theater = createTheater(1L, true);
        when(checkTheaterScreeningLinkPort.existsBlockingScreeningByTheaterId(1L)).thenReturn(false);

        assertThatCode(() -> policy.validateCanDelete(theater))
                .doesNotThrowAnyException();

        verify(checkTheaterScreeningLinkPort).existsBlockingScreeningByTheaterId(1L);
    }

    private Theater createTheater(long theaterId, boolean active) throws Exception {
        when(theaterNameDuplicateValidator.validateNotDuplicate("정책테스트관")).thenReturn(true);

        Theater theater = Theater.Register(
                theaterNameDuplicateValidator,
                "정책테스트관",
                1,
                TheaterType.Standard,
                List.of("A1", "A2", "B1", "B2"),
                2,
                2
        );

        if (!active) {
            Field activeField = Theater.class.getDeclaredField("active");
            activeField.setAccessible(true);
            activeField.set(theater, false);
        }

        Field idField = Theater.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(theater, theaterId);

        return theater;
    }
}
