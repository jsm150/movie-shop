package com.movie.shop.api.operator.domain.aggregate;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest
class OperatorRepositoryIntegrationTest extends AbstractContainerBase {

    @Autowired
    private OperatorRepository operatorRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private OperatorTheaterPermissionScopeTargetPort operatorTheaterPermissionScopeTargetPort;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @Transactional
    @DisplayName("운영자 권한 ADT는 ElementCollection row 단위 JSON payload로 저장되고 재조회 시 타입이 보존된다")
    void saveAndLoad_preservesPermissionTypes() {
        Theater theater = theaterRepository.save(Theater.register(
                "권한 검증용 영화관",
                new TheaterNameUniquenessCondition(true)
        ));

        Operator operator = Operator.register("persist-admin", "{noop}password", "Persist Operator");
        operator.grant(new OperatorPermission.MovieManagePermission());
        operator.grant(new OperatorPermission.TheaterManagePermission(
                TheaterPermissionScope.SingleTheater.create(
                        operatorTheaterPermissionScopeTargetPort.findScopeTarget(theater.getId())
                )
        ));
        operator.grant(new OperatorPermission.ScreeningManagePermission(new TheaterPermissionScope.AllTheaters()));

        Operator savedOperator = operatorRepository.save(operator);

        entityManager.flush();
        entityManager.clear();

        Operator loadedOperator = operatorRepository.getById(savedOperator.getId());
        @SuppressWarnings("unchecked")
        List<Object[]> permissionRows = entityManager.createNativeQuery("""
                        SELECT permission_id,
                               JSON_TYPE(payload_json) AS payload_type,
                               JSON_UNQUOTE(JSON_EXTRACT(payload_json, '$.permissionType')) AS permission_type,
                               JSON_UNQUOTE(JSON_EXTRACT(payload_json, '$.scope.scopeType')) AS scope_type,
                               CAST(JSON_UNQUOTE(JSON_EXTRACT(payload_json, '$.scope.theaterId')) AS UNSIGNED) AS theater_id
                        FROM operator_permission
                        WHERE operator_id = :operatorId
                        ORDER BY permission_id
                        """)
                .setParameter("operatorId", savedOperator.getId())
                .getResultList();

        assertThat(loadedOperator.getPermissions())
                .containsExactlyInAnyOrder(
                        new OperatorPermission.MovieManagePermission(),
                        new OperatorPermission.TheaterManagePermission(
                                TheaterPermissionScope.SingleTheater.create(
                                        operatorTheaterPermissionScopeTargetPort.findScopeTarget(theater.getId())
                                )
                        ),
                        new OperatorPermission.ScreeningManagePermission(new TheaterPermissionScope.AllTheaters())
                );
        assertThat(permissionRows).hasSize(3);
        assertThat(permissionRows)
                .allSatisfy(row -> {
                    assertThat(row[0]).isNotNull();
                    assertThat(row[1]).isEqualTo("OBJECT");
                });
        assertThat(permissionRows)
                .extracting(row -> row[2])
                .containsExactlyInAnyOrder("MOVIE_MANAGE", "THEATER_MANAGE", "SCREENING_MANAGE");
        assertThat(permissionRows)
                .anySatisfy(row -> {
                    assertThat(row[2]).isEqualTo("THEATER_MANAGE");
                    assertThat(row[3]).isEqualTo("SINGLE_THEATER");
                    assertThat(((Number) row[4]).longValue()).isEqualTo(theater.getId());
                })
                .anySatisfy(row -> {
                    assertThat(row[2]).isEqualTo("SCREENING_MANAGE");
                    assertThat(row[3]).isEqualTo("ALL_THEATERS");
                    assertThat(row[4]).isNull();
                });
    }
}
