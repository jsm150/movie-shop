package com.movie.shop.api.theater.api.commands;

import an.awesome.pipelinr.Pipeline;
import com.movie.shop.api.configuration.AbstractContainerBase;
import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.aggregate.TheaterRepository;
import com.movie.shop.api.theater.domain.exceptions.TheaterDomainException;
import com.movie.shop.api.theater.domain.port.TheaterJpaPort;
import com.movie.shop.api.theater.domain.policy.TheaterNamePolicy;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DisplayName("UpdateTheaterCommandHandler 통합 테스트")
class UpdateTheaterCommandHandlerIntegrationTest extends AbstractContainerBase {

    @Autowired
    private Pipeline pipeline;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private TheaterJpaPort theaterJpaPort;

    @Autowired
    private EntityManager entityManager;

    private Theater createAndSaveTheater(String name) {
        TheaterNamePolicy theaterNameDuplicateValidator = new TheaterNamePolicy(theaterJpaPort);
        Theater theater = Theater.register(theaterNameDuplicateValidator, name);
        theater = theaterRepository.save(theater);
        entityManager.flush();
        entityManager.clear();
        return theater;
    }

    @Test
    @Transactional
    @DisplayName("영화관 이름을 수정하면 DB에 변경사항이 반영된다")
    void updateTheaterName_success() {
        Theater theater = createAndSaveTheater("강남점");

        Long resultId = pipeline.send(new UpdateTheaterCommand(theater.getId(), "홍대점"));
        entityManager.flush();
        entityManager.clear();

        Theater updated = theaterRepository.getById(theater.getId());
        assertThat(resultId).isEqualTo(theater.getId());
        assertThat(updated.getName().getName()).isEqualTo("홍대점");
    }

    @Test
    @Transactional
    @DisplayName("동일한 영화관 이름으로 수정하면 성공한다")
    void updateTheaterName_withSameName_success() {
        Theater theater = createAndSaveTheater("강남점");

        pipeline.send(new UpdateTheaterCommand(theater.getId(), "강남점"));
        entityManager.flush();
        entityManager.clear();

        Theater updated = theaterRepository.getById(theater.getId());
        assertThat(updated.getName().getName()).isEqualTo("강남점");
    }

    @Test
    @Transactional
    @DisplayName("중복된 영화관 이름으로 수정하면 실패한다")
    void updateTheaterName_withDuplicateName_fails() {
        createAndSaveTheater("강남점");
        Theater target = createAndSaveTheater("홍대점");

        assertThatThrownBy(() -> pipeline.send(new UpdateTheaterCommand(target.getId(), "강남점")))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("동일한 이름의 영화관이 이미 존재합니다.");
    }

    @Test
    @Transactional
    @DisplayName("존재하지 않는 영화관 ID로 수정하면 실패한다")
    void updateTheater_withNonExistentId_fails() {
        assertThatThrownBy(() -> pipeline.send(new UpdateTheaterCommand(999999L, "없는영화관")))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("영화관 데이터가 존재하지 않습니다.");
    }
}
