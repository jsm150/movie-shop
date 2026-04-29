package com.movie.shop.api.operator.domain.aggregate;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.movie.shop.api.configuration.AbstractContainerBase;
import com.movie.shop.api.operator.domain.aggregate.permission.OperatorPermission;
import com.movie.shop.api.operator.domain.aggregate.permission.TheaterPermissionScope;
import com.movie.shop.api.operator.domain.port.OperatorTheaterPermissionScopeTargetPort;
import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.aggregate.TheaterRepository;
import com.movie.shop.api.theater.domain.condition.TheaterNameUniquenessCondition;
import com.movie.shop.api.theater.domain.port.TheaterJpaPort;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest
class OperatorRepositoryIntegrationTest extends AbstractContainerBase {

    @Autowired
    private OperatorRepository operatorRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private TheaterJpaPort theaterJpaPort;

    @Autowired
    private OperatorTheaterPermissionScopeTargetPort operatorTheaterPermissionScopeTargetPort;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @Transactional
    @DisplayName("운영자 권한 ADT는 JSON으로 저장되고 재조회 시 타입이 보존된다")
    void saveAndLoad_preservesPermissionTypes() {
        Theater theater = theaterRepository.save(Theater.register(
                "권한 검증용 영화관",
                new TheaterNameUniquenessCondition(true)
        ));

        Operator operator = Operator.register("persist-admin", "{noop}password", "Persist Operator");
        operator.grant(new OperatorPermission.MovieManagePermission());
        operator.grant(new OperatorPermission.TheaterManagePermission(
                TheaterPermissionScope.SingleTheater.create(
                        theater.getId(),
                        operatorTheaterPermissionScopeTargetPort.findScopeTarget(theater.getId())
                )
        ));
        operator.grant(new OperatorPermission.ScreeningManagePermission(new TheaterPermissionScope.AllTheaters()));

        Operator savedOperator = operatorRepository.save(operator);

        entityManager.flush();
        entityManager.clear();

        Operator loadedOperator = operatorRepository.getById(savedOperator.getId());

        assertThat(loadedOperator.getPermissions())
                .containsExactlyInAnyOrder(
                        new OperatorPermission.MovieManagePermission(),
                        new OperatorPermission.TheaterManagePermission(
                                TheaterPermissionScope.SingleTheater.create(
                                        theater.getId(),
                                        operatorTheaterPermissionScopeTargetPort.findScopeTarget(theater.getId())
                                )
                        ),
                        new OperatorPermission.ScreeningManagePermission(new TheaterPermissionScope.AllTheaters())
                );
    }
}
