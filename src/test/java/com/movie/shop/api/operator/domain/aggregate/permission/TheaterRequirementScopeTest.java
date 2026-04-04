package com.movie.shop.api.operator.domain.aggregate.permission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.movie.shop.api.operator.domain.exceptions.OperatorDomainException;
import com.movie.shop.api.operator.domain.policy.TheaterScopeCreationPolicy;

class TheaterRequirementScopeTest {

    @Test
    @DisplayName("양수 영화관 식별자로 요구 SingleTheater를 생성할 수 있다")
    void create_withPositiveTheaterId_succeeds() {
        assertThatNoException()
                .isThrownBy(() -> {
                    TheaterRequirementScope.SingleTheater scope = new TheaterRequirementScope.SingleTheater(1L);

                    assertThat(scope.theaterId()).isEqualTo(1L);
                });
    }

    @Test
    @DisplayName("0 이하의 영화관 식별자로 요구 SingleTheater를 생성할 수 없다")
    void create_withNonPositiveTheaterId_fails() {
        assertThatThrownBy(() -> new TheaterRequirementScope.SingleTheater(0L))
                .isInstanceOf(OperatorDomainException.class)
                .hasMessage("영화관 식별자는 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("전체 영화관 요구 범위는 전체 영화관 권한 범위만 만족한다")
    void allTheaters_isSatisfiedByOnlyAllTheatersPermissionScope() {
        TheaterRequirementScope requirementScope = new TheaterRequirementScope.AllTheaters();

        assertThat(requirementScope.isSatisfiedBy(new TheaterPermissionScope.AllTheaters())).isTrue();
        assertThat(requirementScope.isSatisfiedBy(permissionSingleTheater(1L))).isFalse();
    }

    @Test
    @DisplayName("단일 영화관 요구 범위는 전체 영화관 권한 또는 같은 단일 영화관 권한으로 만족한다")
    void singleTheater_isSatisfiedByAllTheatersOrSameSingleTheaterPermissionScope() {
        TheaterRequirementScope requirementScope = new TheaterRequirementScope.SingleTheater(1L);

        assertThat(requirementScope.isSatisfiedBy(new TheaterPermissionScope.AllTheaters())).isTrue();
        assertThat(requirementScope.isSatisfiedBy(permissionSingleTheater(1L))).isTrue();
        assertThat(requirementScope.isSatisfiedBy(permissionSingleTheater(2L))).isFalse();
    }

    private TheaterPermissionScope.SingleTheater permissionSingleTheater(long theaterId) {
        return TheaterPermissionScope.SingleTheater.create(
                theaterId,
                new TheaterScopeCreationPolicy(existingTheaterId -> existingTheaterId == theaterId)
        );
    }
}
