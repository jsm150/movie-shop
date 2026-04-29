package com.movie.shop.api.auditorium.domain.aggregate;

import com.movie.shop.api.auditorium.domain.condition.AuditoriumNameUniquenessCondition;
import com.movie.shop.api.auditorium.domain.condition.AuditoriumOperatingTheaterStatus;
import com.movie.shop.api.auditorium.domain.condition.AuditoriumRegistrationTheater;
import com.movie.shop.api.auditorium.domain.condition.AuditoriumScreeningPresence;
import com.movie.shop.api.auditorium.domain.exceptions.AuditoriumDomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Auditorium 단위 테스트")
class AuditoriumTest {

    private AuditoriumNameUniquenessCondition uniqueNameCondition;
    private AuditoriumNameUniquenessCondition duplicateNameCondition;
    private AuditoriumScreeningPresence blockingScreeningPresence;
    private AuditoriumScreeningPresence emptyScreeningPresence;
    private Optional<AuditoriumOperatingTheaterStatus> activeTheaterStatus;
    private Optional<AuditoriumOperatingTheaterStatus> inactiveTheaterStatus;
    private Optional<AuditoriumOperatingTheaterStatus> missingTheaterStatus;
    private long theaterId;
    private String validName;
    private int validFloor;
    private AuditoriumType validType;
    private List<String> validSeats;
    private int validRowCount;
    private int validColumnCount;

    @BeforeEach
    void setUp() {
        uniqueNameCondition = new AuditoriumNameUniquenessCondition(true);
        duplicateNameCondition = new AuditoriumNameUniquenessCondition(false);
        blockingScreeningPresence = new AuditoriumScreeningPresence(true);
        emptyScreeningPresence = new AuditoriumScreeningPresence(false);
        activeTheaterStatus = Optional.of(new AuditoriumOperatingTheaterStatus(true));
        inactiveTheaterStatus = Optional.of(new AuditoriumOperatingTheaterStatus(false));
        missingTheaterStatus = Optional.empty();
        theaterId = 1L;
        validName = "1관";
        validFloor = 1;
        validType = AuditoriumType.Standard;
        validSeats = List.of("A1", "A2", "A3", "B1", "B2", "B3");
        validRowCount = 2;
        validColumnCount = 3;
    }

    @Test
    @DisplayName("유효한 데이터로 등록 성공한다")
    void register_withValidData_success() {
        Auditorium auditorium = createAuditorium();

        assertThat(auditorium.getTheaterId()).isEqualTo(theaterId);
        assertThat(auditorium.getName().getName()).isEqualTo(validName);
        assertThat(auditorium.getFloor()).isEqualTo(validFloor);
        assertThat(auditorium.getAuditoriumType()).isEqualTo(validType);
        assertThat(auditorium.getSeats().getSeats()).containsExactlyElementsOf(validSeats);
        assertThat(auditorium.isActive()).isTrue();
    }

    @Test
    @DisplayName("영화관 ID가 0 이하면 등록 실패한다")
    void register_withInvalidTheaterId_fail() {
        assertThatThrownBy(() -> Auditorium.register(
                uniqueNameCondition,
                registrationTheater(),
                0L,
                validName,
                validFloor,
                validType,
                validSeats,
                validRowCount,
                validColumnCount
        ))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("영화관 ID는 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("존재하지 않는 영화관에는 상영관 등록이 실패한다")
    void register_whenTheaterMissing_fail() {
        assertThatThrownBy(() -> Auditorium.register(
                uniqueNameCondition,
                Optional.empty(),
                theaterId,
                validName,
                validFloor,
                validType,
                validSeats,
                validRowCount,
                validColumnCount
        ))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("존재하지 않는 영화관");
    }

    @Test
    @DisplayName("중복된 이름으로 등록하면 실패한다")
    void register_whenNameDuplicated_fail() {
        assertThatThrownBy(() -> Auditorium.register(
                duplicateNameCondition,
                registrationTheater(),
                theaterId,
                validName,
                validFloor,
                validType,
                validSeats,
                validRowCount,
                validColumnCount
        ))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("이미 존재합니다.");
    }

    @Test
    @DisplayName("층수가 -10 미만이면 등록 실패한다")
    void register_withFloorTooSmall_fail() {
        assertThatThrownBy(() -> Auditorium.register(
                uniqueNameCondition,
                registrationTheater(),
                theaterId,
                validName,
                -11,
                validType,
                validSeats,
                validRowCount,
                validColumnCount
        ))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("층수는 -10에서 100 사이여야 합니다.");
    }

    @Test
    @DisplayName("층수가 100 초과면 등록 실패한다")
    void register_withFloorTooLarge_fail() {
        assertThatThrownBy(() -> Auditorium.register(
                uniqueNameCondition,
                registrationTheater(),
                theaterId,
                validName,
                101,
                validType,
                validSeats,
                validRowCount,
                validColumnCount
        ))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("층수는 -10에서 100 사이여야 합니다.");
    }

    @Test
    @DisplayName("활성 상태이면 상영 가능 여부가 true를 반환한다")
    void canHostScreening_whenActive_returnsTrue() {
        Auditorium auditorium = createAuditorium();

        assertThat(auditorium.canHostScreening()).isTrue();
    }

    @Test
    @DisplayName("비활성 상태이면 상영 가능 여부가 false를 반환한다")
    void canHostScreening_whenInactive_returnsFalse() {
        Auditorium auditorium = createAuditorium();

        auditorium.changeStatus(AuditoriumStatusChange.DEACTIVATE, emptyScreeningPresence, missingTheaterStatus);

        assertThat(auditorium.canHostScreening()).isFalse();
    }

    @Test
    @DisplayName("DEACTIVATE 요청 시 차단 상영이 있으면 예외가 발생한다")
    void changeActive_whenDeactivateBlocked_throwsException() {
        Auditorium auditorium = createAuditorium();

        assertThatThrownBy(() -> auditorium.changeStatus(
                AuditoriumStatusChange.DEACTIVATE,
                blockingScreeningPresence,
                missingTheaterStatus
        ))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("비활성화할 수 없습니다.");
    }

    @Test
    @DisplayName("DEACTIVATE 요청 시 차단 상영이 없으면 비활성 상태로 변경된다")
    void changeActive_whenDeactivateAllowed_becomesInactive() {
        Auditorium auditorium = createAuditorium();

        auditorium.changeStatus(AuditoriumStatusChange.DEACTIVATE, emptyScreeningPresence, missingTheaterStatus);

        assertThat(auditorium.isActive()).isFalse();
    }

    @Test
    @DisplayName("이미 비활성인 상영관은 DEACTIVATE 시 차단 상영이 있어도 통과한다")
    void changeActive_whenAlreadyInactiveAndDeactivate_doesNotThrow() throws Exception {
        Auditorium auditorium = createAuditorium();
        setActive(auditorium, false);

        assertThatCode(() -> auditorium.changeStatus(
                AuditoriumStatusChange.DEACTIVATE,
                blockingScreeningPresence,
                missingTheaterStatus
        ))
                .doesNotThrowAnyException();
        assertThat(auditorium.isActive()).isFalse();
    }

    @Test
    @DisplayName("ACTIVATE 요청 시 연결 영화관이 활성 상태면 성공한다")
    void changeActive_whenActivateAndTheaterActive_success() throws Exception {
        Auditorium auditorium = createAuditorium();
        setActive(auditorium, false);

        auditorium.changeStatus(AuditoriumStatusChange.ACTIVATE, emptyScreeningPresence, activeTheaterStatus);

        assertThat(auditorium.isActive()).isTrue();
    }

    @Test
    @DisplayName("ACTIVATE 요청 시 연결 영화관이 비활성이면 실패한다")
    void changeActive_whenActivateAndTheaterInactive_fail() throws Exception {
        Auditorium auditorium = createAuditorium();
        setActive(auditorium, false);

        assertThatThrownBy(() -> auditorium.changeStatus(
                AuditoriumStatusChange.ACTIVATE,
                emptyScreeningPresence,
                inactiveTheaterStatus
        ))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("비활성화된 영화관의 상영관은 활성화할 수 없습니다.");
    }

    @Test
    @DisplayName("ACTIVATE 요청 시 연결 영화관 정보가 없으면 실패한다")
    void changeActive_whenActivateAndTheaterMissing_fail() throws Exception {
        Auditorium auditorium = createAuditorium();
        setActive(auditorium, false);

        assertThatThrownBy(() -> auditorium.changeStatus(
                AuditoriumStatusChange.ACTIVATE,
                emptyScreeningPresence,
                missingTheaterStatus
        ))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("영화관 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("상태 변경 요청이 null이면 예외가 발생한다")
    void changeActive_whenStatusNull_throwsException() {
        Auditorium auditorium = createAuditorium();

        assertThatThrownBy(() -> auditorium.changeStatus(null, emptyScreeningPresence, missingTheaterStatus))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("활성 상태는 필수");
    }

    @Test
    @DisplayName("차단 상영이 있는 상영관은 삭제할 수 없다")
    void validateCanDelete_whenBlockingScreeningExists_throwsException() {
        Auditorium auditorium = createAuditorium();

        assertThatThrownBy(() -> auditorium.validateCanDelete(blockingScreeningPresence))
                .isInstanceOf(AuditoriumDomainException.class)
                .hasMessageContaining("삭제할 수 없습니다.");
    }

    @Test
    @DisplayName("차단 상영이 없으면 삭제 검증에 성공한다")
    void validateCanDelete_whenNoBlockingScreening_doesNotThrow() {
        Auditorium auditorium = createAuditorium();

        assertThatCode(() -> auditorium.validateCanDelete(emptyScreeningPresence))
                .doesNotThrowAnyException();
    }

    private Auditorium createAuditorium() {
        return Auditorium.register(
                uniqueNameCondition,
                registrationTheater(),
                theaterId,
                validName,
                validFloor,
                validType,
                validSeats,
                validRowCount,
                validColumnCount
        );
    }

    private Optional<AuditoriumRegistrationTheater> registrationTheater() {
        return Optional.of(new AuditoriumRegistrationTheater(theaterId));
    }

    private void setActive(Auditorium auditorium, boolean active) throws Exception {
        Field activeField = Auditorium.class.getDeclaredField("active");
        activeField.setAccessible(true);
        activeField.set(auditorium, active);
    }
}
