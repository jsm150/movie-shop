package com.movie.shop.api.movie.domain.aggregate;

import com.movie.shop.api.movie.domain.exceptions.MovieDomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActorTest {

    private String validName;
    private OffsetDateTime validDateOfBirth;
    private String validNational;
    private String validRole;

    @BeforeEach
    void setUp() {
        validName = "매튜 매코너히";
        validDateOfBirth = OffsetDateTime.parse("1969-11-04T00:00:00Z");
        validNational = "USA";
        validRole = "쿠퍼";
    }

    @Test
    @DisplayName("유효한 배우 정보를 생성하면 입력한 값으로 생성된다")
    void createActor_withValidData_succeeds() {
        Actor actor = new Actor(
                validName,
                validDateOfBirth,
                validNational,
                validRole
        );

        assertThat(actor.getName()).isEqualTo(validName);
        assertThat(actor.getDateOfBirth()).isEqualTo(validDateOfBirth);
        assertThat(actor.getNational()).isEqualTo(validNational);
        assertThat(actor.getRole()).isEqualTo(validRole);
    }

    @Test
    @DisplayName("배우 이름이 빈 값이면 생성 시 예외가 발생한다")
    void createActor_withBlankName_throwsException() {
        assertThatThrownBy(() -> new Actor(
                "",
                validDateOfBirth,
                validNational,
                validRole
        ))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("배우 이름은 필수입니다");
    }

    @Test
    @DisplayName("배우 이름이 null이면 생성 시 예외가 발생한다")
    void createActor_withNullName_throwsException() {
        assertThatThrownBy(() -> new Actor(
                null,
                validDateOfBirth,
                validNational,
                validRole
        ))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("배우 이름은 필수입니다");
    }

    @Test
    @DisplayName("배우 이름이 100자를 초과하면 생성 시 예외가 발생한다")
    void createActor_withTooLongName_throwsException() {
        assertThatThrownBy(() -> new Actor(
                "a".repeat(101),
                validDateOfBirth,
                validNational,
                validRole
        ))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("배우 이름은 100자를 초과할 수 없습니다");
    }

    @Test
    @DisplayName("배우 생년월일이 null이면 생성 시 예외가 발생한다")
    void createActor_withNullDateOfBirth_throwsException() {
        assertThatThrownBy(() -> new Actor(
                validName,
                null,
                validNational,
                validRole
        ))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("배우의 생년월일은 필수입니다");
    }

    @Test
    @DisplayName("배우 생년월일이 미래면 생성 시 예외가 발생한다")
    void createActor_withFutureDateOfBirth_throwsException() {
        OffsetDateTime futureDate = OffsetDateTime.now().plusDays(1);

        assertThatThrownBy(() -> new Actor(
                validName,
                futureDate,
                validNational,
                validRole
        ))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("배우의 생년월일은 과거여야 합니다");
    }

    @Test
    @DisplayName("배우 국적이 빈 값이면 생성 시 예외가 발생한다")
    void createActor_withBlankNational_throwsException() {
        assertThatThrownBy(() -> new Actor(
                validName,
                validDateOfBirth,
                "",
                validRole
        ))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("배우의 국적은 필수입니다");
    }

    @Test
    @DisplayName("배우 국적이 null이면 생성 시 예외가 발생한다")
    void createActor_withNullNational_throwsException() {
        assertThatThrownBy(() -> new Actor(
                validName,
                validDateOfBirth,
                null,
                validRole
        ))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("배우의 국적은 필수입니다");
    }

    @Test
    @DisplayName("배우 국적이 100자를 초과하면 생성 시 예외가 발생한다")
    void createActor_withTooLongNational_throwsException() {
        assertThatThrownBy(() -> new Actor(
                validName,
                validDateOfBirth,
                "a".repeat(101),
                validRole
        ))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("배우의 국적은 100자를 초과할 수 없습니다");
    }

    @Test
    @DisplayName("배우 역할이 빈 값이면 생성 시 예외가 발생한다")
    void createActor_withBlankRole_throwsException() {
        assertThatThrownBy(() -> new Actor(
                validName,
                validDateOfBirth,
                validNational,
                ""
        ))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("배우의 역할은 필수입니다");
    }

    @Test
    @DisplayName("배우 역할이 null이면 생성 시 예외가 발생한다")
    void createActor_withNullRole_throwsException() {
        assertThatThrownBy(() -> new Actor(
                validName,
                validDateOfBirth,
                validNational,
                null
        ))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("배우의 역할은 필수입니다");
    }

    @Test
    @DisplayName("배우 역할이 100자를 초과하면 생성 시 예외가 발생한다")
    void createActor_withTooLongRole_throwsException() {
        assertThatThrownBy(() -> new Actor(
                validName,
                validDateOfBirth,
                validNational,
                "a".repeat(101)
        ))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("배우의 역할은 100자를 초과할 수 없습니다");
    }

    @Test
    @DisplayName("여러 어노테이션 검증 오류를 한 번에 반환한다")
    void createActor_withMultipleValidationErrors_collectsErrors() {
        assertThatThrownBy(() -> new Actor("", null, "", ""))
                .isInstanceOf(MovieDomainException.class)
                .satisfies(exception -> assertThat(
                        ((MovieDomainException) exception).getErrors()
                )
                        .contains(
                                "배우 이름은 필수입니다.",
                                "배우의 생년월일은 필수입니다.",
                                "배우의 국적은 필수입니다.",
                                "배우의 역할은 필수입니다."
                        ));
    }
}
