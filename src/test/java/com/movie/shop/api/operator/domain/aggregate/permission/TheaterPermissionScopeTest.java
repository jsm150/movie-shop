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
                            Optional.of(new OperatorTheaterPermissionScopeTarget(1L))
                    );

                    assertThat(scope.getTheaterId()).isEqualTo(1L);
                });
    }

    @Test
    @DisplayName("존재하지 않는 영화관 식별자로 권한 SingleTheater를 생성할 수 없다")
    void create_withMissingTheater_fails() {
        assertThatThrownBy(() -> TheaterPermissionScope.SingleTheater.create(
                Optional.empty()
        ))
                .isInstanceOf(OperatorDomainException.class)
                .hasMessage("존재하지 않는 영화관으로 권한 범위를 생성할 수 없습니다.");
    }

    @Test
    @DisplayName("권한 범위 대상이 null이면 권한 SingleTheater를 생성할 수 없다")
    void create_withNullScopeTarget_fails() {
        assertThatThrownBy(() -> TheaterPermissionScope.SingleTheater.create(null))
                .isInstanceOf(OperatorDomainException.class)
                .hasMessage("영화관 권한 범위 대상은 필수입니다.");
    }
}
