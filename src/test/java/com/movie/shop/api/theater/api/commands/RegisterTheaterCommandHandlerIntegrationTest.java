package com.movie.shop.api.theater.api.commands;

import an.awesome.pipelinr.Pipeline;
import com.movie.shop.api.configuration.AbstractContainerBase;
import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.aggregate.TheaterRepository;
import com.movie.shop.api.theater.domain.exceptions.TheaterDomainException;
import com.movie.shop.api.theater.domain.port.TheaterJpaPort;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DisplayName("RegisterTheaterCommandHandler 통합 테스트")
class RegisterTheaterCommandHandlerIntegrationTest extends AbstractContainerBase {

    @Autowired
    private Pipeline pipeline;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private TheaterJpaPort theaterJpaPort;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    @DisplayName("영화관을 등록하면 DB에 값이 저장된다")
    void registerTheater_persistsToDatabase() {
        RegisterTheaterCommand command = new RegisterTheaterCommand("강남점");

        Long theaterId = pipeline.send(command);
        entityManager.flush();
        entityManager.clear();

        Theater theater = theaterJpaPort.findById(theaterId).orElseThrow();
        assertThat(theater.getName().getName()).isEqualTo("강남점");
        assertThat(theater.isActive()).isTrue();
    }

    @Test
    @Transactional
    @DisplayName("동일한 영화관 이름으로 등록하면 실패한다")
    void registerTheater_withDuplicateName_fails() {
        pipeline.send(new RegisterTheaterCommand("강남점"));
        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> pipeline.send(new RegisterTheaterCommand("강남점")))
                .isInstanceOf(TheaterDomainException.class)
                .hasMessageContaining("'강남점' 이름의 영화관이 이미 존재합니다.");

        assertThat(theaterJpaPort.count()).isEqualTo(1);
    }
}
