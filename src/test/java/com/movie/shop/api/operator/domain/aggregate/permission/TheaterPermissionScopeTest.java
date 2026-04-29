package com.movie.shop.api.operator.domain.aggregate.permission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.movie.shop.api.operator.domain.condition.OperatorTheaterPermissionScopeTarget;
import com.movie.shop.api.operator.domain.exceptions.OperatorDomainException;

import java.util.Optional;

class TheaterPermissionScopeTest {

    @Test
    @DisplayName("존재하는 영화관 식별자로 권한 SingleTheater를 생성할 수 있다")
    void create_withExistingTheater_succeeds() {
        assertThatNoException()
                .isThrownBy(() -> {
                    TheaterPermissionScope.SingleTheater scope = TheaterPermissionScope.SingleTheater.create(
                            1L,
                            Optional.of(new OperatorTheaterPermissionScopeTarget(1L))
                    );

                    assertThat(scope.getTheaterId()).isEqualTo(1L);
                });
    }

    @Test
    @DisplayName("존재하지 않는 영화관 식별자로 권한 SingleTheater를 생성할 수 없다")
    void create_withMissingTheater_fails() {
        assertThatThrownBy(() -> TheaterPermissionScope.SingleTheater.create(
                2L,
                Optional.empty()
        ))
                .isInstanceOf(OperatorDomainException.class)
                .hasMessage("존재하지 않는 영화관으로 권한 범위를 생성할 수 없습니다.");
    }

    @Test
    @DisplayName("0 이하의 영화관 식별자로 권한 SingleTheater를 생성할 수 없다")
    void create_withNonPositiveTheaterId_fails() {
        assertThatThrownBy(() -> TheaterPermissionScope.SingleTheater.create(
                0L,
                Optional.of(new OperatorTheaterPermissionScopeTarget(0L))
        ))
                .isInstanceOf(OperatorDomainException.class)
                .hasMessage("영화관 식별자는 0보다 커야 합니다.");
    }
}
