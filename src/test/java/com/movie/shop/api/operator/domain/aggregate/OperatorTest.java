package com.movie.shop.api.operator.domain.aggregate;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.movie.shop.api.operator.domain.aggregate.permission.OperatorAuthorizationRequirement;
import com.movie.shop.api.operator.domain.aggregate.permission.OperatorPermission;
import com.movie.shop.api.operator.domain.aggregate.permission.TheaterPermissionScope;
import com.movie.shop.api.operator.domain.aggregate.permission.TheaterRequirementScope;
import com.movie.shop.api.operator.domain.condition.OperatorPasswordVerification;
import com.movie.shop.api.operator.domain.condition.OperatorTheaterPermissionScopeTarget;
import com.movie.shop.api.operator.domain.exceptions.OperatorDomainException;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Optional;

class OperatorTest {

    @Test
    @DisplayName("영화 관리 권한이 있으면 영화 관리 요구사항을 통과한다")
    void authorize_withMovieManagePermission_succeeds() {
        Operator operator = registerOperator();
        operator.grant(new OperatorPermission.MovieManagePermission());

        assertThatNoException()
                .isThrownBy(() -> operator.authorize(new OperatorAuthorizationRequirement.RequireMovieManage()));
    }

    @Test
    @DisplayName("특정 영화관 범위의 상영관 관리 권한은 같은 영화관에만 허용된다")
    void authorize_withSingleTheaterScope_onlyAllowsSameTheater() {
        Operator operator = registerOperator();
        operator.grant(new OperatorPermission.AuditoriumManagePermission(permissionSingleTheater(1L)));

        assertThatNoException()
                .isThrownBy(() -> operator.authorize(
                        new OperatorAuthorizationRequirement.RequireAuditoriumManage(requirementSingleTheater(1L))
                ));

        assertThatThrownBy(() -> operator.authorize(
                new OperatorAuthorizationRequirement.RequireAuditoriumManage(requirementSingleTheater(2L))
        ))
                .isInstanceOf(OperatorDomainException.class)
                .hasMessage("운영자 권한이 없습니다.");
    }

    @Test
    @DisplayName("전체 영화관 범위 권한은 특정 영화관 요구사항을 통과한다")
    void authorize_withAllTheatersScope_succeedsForSingleTheaterRequirement() {
        Operator operator = registerOperator();
        operator.grant(new OperatorPermission.ScreeningManagePermission(new TheaterPermissionScope.AllTheaters()));

        assertThatNoException()
                .isThrownBy(() -> operator.authorize(
                        new OperatorAuthorizationRequirement.RequireScreeningManage(requirementSingleTheater(1L))
                ));
        assertThatNoException()
                .isThrownBy(() -> operator.authorize(
                        new OperatorAuthorizationRequirement.RequireScreeningManage(requirementSingleTheater(999L))
                ));
    }

    @Test
    @DisplayName("전체 영화관 관리 권한은 전체 영화관 관리 요구사항을 통과한다")
    void authorize_withAllTheatersPermission_succeedsForAllTheatersRequirement() {
        Operator operator = registerOperator();
        operator.grant(new OperatorPermission.TheaterManagePermission(new TheaterPermissionScope.AllTheaters()));

        assertThatNoException()
                .isThrownBy(() -> operator.authorize(
                        new OperatorAuthorizationRequirement.RequireTheaterManage(new TheaterRequirementScope.AllTheaters())
                ));
    }

    @Test
    @DisplayName("특정 영화관 관리 권한은 전체 영화관 관리 요구사항을 통과하지 못한다")
    void authorize_withSingleTheaterPermission_failsForAllTheatersRequirement() {
        Operator operator = registerOperator();
        operator.grant(new OperatorPermission.TheaterManagePermission(permissionSingleTheater(1L)));

        assertThatThrownBy(() -> operator.authorize(
                new OperatorAuthorizationRequirement.RequireTheaterManage(new TheaterRequirementScope.AllTheaters())
        ))
                .isInstanceOf(OperatorDomainException.class)
                .hasMessage("운영자 권한이 없습니다.");
    }

    @Test
    @DisplayName("전체 영화관 관리 권한은 특정 영화관 관리 요구사항을 통과한다")
    void authorize_withAllTheatersPermission_succeedsForSingleTheaterRequirement() {
        Operator operator = registerOperator();
        operator.grant(new OperatorPermission.TheaterManagePermission(new TheaterPermissionScope.AllTheaters()));

        assertThatNoException()
                .isThrownBy(() -> operator.authorize(
                        new OperatorAuthorizationRequirement.RequireTheaterManage(requirementSingleTheater(1L))
                ));
    }

    @Test
    @DisplayName("비활성 운영자는 권한이 있어도 인가에 실패한다")
    void authorize_withSuspendedOperator_fails() {
        Operator operator = registerOperator();
        operator.grant(new OperatorPermission.MovieManagePermission());
        operator.suspend();

        assertThatThrownBy(() -> operator.authorize(new OperatorAuthorizationRequirement.RequireMovieManage()))
                .isInstanceOf(OperatorDomainException.class)
                .hasMessage("비활성화된 운영자는 권한을 행사할 수 없습니다.");
    }

    @Test
    @DisplayName("같은 권한을 중복 부여할 수 없다")
    void grant_withDuplicatePermission_fails() {
        Operator operator = registerOperator();
        OperatorPermission permission = new OperatorPermission.OperatorManagePermission();
        operator.grant(permission);

        assertThatThrownBy(() -> operator.grant(permission))
                .isInstanceOf(OperatorDomainException.class)
                .hasMessage("이미 부여된 권한입니다.");
    }

    @Test
    @DisplayName("권한을 회수하면 더 이상 같은 요구사항을 통과하지 못한다")
    void revoke_thenAuthorizeFails() {
        Operator operator = registerOperator();
        OperatorPermission permission = new OperatorPermission.MovieManagePermission();
        operator.grant(permission);
        operator.revoke(permission);

        assertThatThrownBy(() -> operator.authorize(new OperatorAuthorizationRequirement.RequireMovieManage()))
                .isInstanceOf(OperatorDomainException.class)
                .hasMessage("운영자 권한이 없습니다.");
    }

    @Test
    @DisplayName("활성 운영자는 비밀번호 검증이 성공하면 인증에 성공한다")
    void authenticate_withMatchedPassword_succeeds() {
        Operator operator = registerOperator();

        assertThatNoException()
                .isThrownBy(() -> operator.authenticate(new OperatorPasswordVerification(true)));
    }

    @Test
    @DisplayName("비밀번호 검증이 실패하면 인증에 실패한다")
    void authenticate_withMismatchedPassword_fails() {
        Operator operator = registerOperator();

        assertThatThrownBy(() -> operator.authenticate(new OperatorPasswordVerification(false)))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("로그인 ID 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("비활성 운영자는 인증에 실패한다")
    void authenticate_withSuspendedOperator_fails() {
        Operator operator = registerOperator();
        operator.suspend();

        assertThatThrownBy(() -> operator.authenticate(new OperatorPasswordVerification(true)))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("비활성화된 계정입니다.");
    }

    private Operator registerOperator() {
        return Operator.register("operator", "{noop}password", "Operator");
    }

    private TheaterPermissionScope.SingleTheater permissionSingleTheater(long theaterId) {
        return TheaterPermissionScope.SingleTheater.create(
                theaterId,
                Optional.of(new OperatorTheaterPermissionScopeTarget(theaterId))
        );
    }

    private TheaterRequirementScope.SingleTheater requirementSingleTheater(long theaterId) {
        return new TheaterRequirementScope.SingleTheater(theaterId);
    }
}
