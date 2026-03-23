package com.movie.shop.api.theater.api.commands;

import an.awesome.pipelinr.Pipeline;
import com.movie.shop.api.configuration.AbstractContainerBase;
import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.aggregate.TheaterActiveChange;
import com.movie.shop.api.theater.domain.aggregate.TheaterRepository;
import com.movie.shop.api.theater.domain.exceptions.TheaterDomainException;
import com.movie.shop.api.theater.domain.policy.TheaterNameDuplicateValidator;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DisplayName("ChangeActiveTheaterCommandHandler 통합 테스트")
class ChangeActiveTheaterCommandHandlerIntegrationTest extends AbstractContainerBase {

    @Autowired
    private Pipeline pipeline;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private TheaterNameDuplicateValidator theaterNameDuplicateValidator;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Theater createAndSaveTheater(String name) {
        Theater theater = Theater.register(theaterNameDuplicateValidator, name);
        theater = theaterRepository.save(theater);
        entityManager.flush();
        entityManager.clear();
        return theater;
    }

    private void insertAuditorium(long theaterId, String name) {
        jdbcTemplate.update(
                """
                INSERT INTO auditorium
                (theater_id, name, floor, auditorium_type, is_active, seats, row_count, column_count)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                theaterId,
                name,
                1,
                "Standard",
                true,
                "[\"A1\",\"A2\"]",
                1,
                2
        );
    }

    @Test
    @Transactional
    @DisplayName("활성화된 영화관을 비활성화한다")
    void changeActive_deactivate_success() {
        Theater theater = createAndSaveTheater("강남점");

        pipeline.send(new ChangeActiveTheaterCommand(theater.getId(), TheaterActiveChange.DEACTIVATE));
        entityManager.flush();
        entityManager.clear();

        Theater updated = theaterRepository.getById(theater.getId());
        assertThat(updated.isActive()).isFalse();
    }

    @Test
    @Transactional
    @DisplayName("비활성화된 영화관을 활성화한다")
    void changeActive_activate_success() {
        Theater theater = createAndSaveTheater("홍대점");
        pipeline.send(new ChangeActiveTheaterCommand(theater.getId(), TheaterActiveChange.DEACTIVATE));

        pipeline.send(new ChangeActiveTheaterCommand(theater.getId(), TheaterActiveChange.ACTIVATE));
        entityManager.flush();
        entityManager.clear();

        Theater updated = theaterRepository.getById(theater.getId());
        assertThat(updated.isActive()).isTrue();
    }

    @Test
    @Transactional
    @DisplayName("존재하지 않는 영화관 ID로 상태를 변경하면 실패한다")
    void changeActive_withNonExistentId_fails() {
        assertThatThrownBy(() -> pipeline.send(new ChangeActiveTheaterCommand(999999L, TheaterActiveChange.DEACTIVATE)))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("영화관 데이터가 존재하지 않습니다.");
    }

    @Test
    @Transactional
    @DisplayName("연결된 상영관이 있는 영화관은 비활성화할 수 없다")
    void changeActive_withLinkedAuditorium_fails() {
        Theater theater = createAndSaveTheater("잠실점");
        insertAuditorium(theater.getId(), "1관");

        assertThatThrownBy(() -> pipeline.send(new ChangeActiveTheaterCommand(theater.getId(), TheaterActiveChange.DEACTIVATE)))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("비활성화할 수 없습니다.");

        Theater unchanged = theaterRepository.getById(theater.getId());
        assertThat(unchanged.isActive()).isTrue();
    }
}
