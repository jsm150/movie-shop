package com.movie.shop.api.auditorium.api.commands;

import an.awesome.pipelinr.Pipeline;
import com.movie.shop.api.auditorium.domain.aggregate.Auditorium;
import com.movie.shop.api.auditorium.domain.aggregate.AuditoriumRepository;
import com.movie.shop.api.auditorium.domain.aggregate.AuditoriumType;
import com.movie.shop.api.auditorium.domain.policy.AuditoriumNameDuplicatePolicy;
import com.movie.shop.api.auditorium.domain.policy.AuditoriumTheaterExistencePolicy;
import com.movie.shop.api.auditorium.domain.policy.status.AuditoriumTheaterExistenceStatus;
import com.movie.shop.api.auditorium.domain.port.AuditoriumJpaPort;
import com.movie.shop.api.configuration.AbstractContainerBase;
import com.movie.shop.api.theater.api.commands.ChangeActiveTheaterCommand;
import com.movie.shop.api.theater.domain.aggregate.Theater;
import com.movie.shop.api.theater.domain.aggregate.TheaterActiveChange;
import com.movie.shop.api.theater.domain.aggregate.TheaterRepository;
import com.movie.shop.api.theater.domain.port.TheaterJpaPort;
import com.movie.shop.api.theater.domain.policy.TheaterNamePolicy;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.OffsetDateTime;
import java.util.List;

abstract class AuditoriumCommandIntegrationTestSupport extends AbstractContainerBase {

    @Autowired
    protected Pipeline pipeline;

    @Autowired
    protected TheaterRepository theaterRepository;

    @Autowired
    protected TheaterJpaPort theaterJpaPort;

    @Autowired
    protected AuditoriumRepository auditoriumRepository;

    @Autowired
    protected AuditoriumJpaPort auditoriumJpaPort;

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected Theater createAndSaveTheater(String name, boolean active) {
        TheaterNamePolicy theaterNameDuplicateValidator = new TheaterNamePolicy(theaterJpaPort);
        Theater theater = Theater.register(theaterNameDuplicateValidator, name);
        theater = theaterRepository.save(theater);
        flushAndClear();

        if (!active) {
            pipeline.send(new ChangeActiveTheaterCommand(theater.getId(), TheaterActiveChange.DEACTIVATE));
            flushAndClear();
            theater = theaterRepository.getById(theater.getId());
        }

        return theater;
    }

    protected Auditorium createAndSaveAuditorium(long theaterId, String name) {
        AuditoriumNameDuplicatePolicy auditoriumNameDuplicatePolicy = new AuditoriumNameDuplicatePolicy(
                auditoriumJpaPort.loadNameDuplication(theaterId, name)
        );
        Auditorium auditorium = Auditorium.register(
                auditoriumNameDuplicatePolicy,
                new AuditoriumTheaterExistencePolicy(new AuditoriumTheaterExistenceStatus(true)),
                theaterId,
                name,
                1,
                AuditoriumType.Standard,
                List.of("A1", "A2"),
                1,
                2
        );
        auditorium = auditoriumRepository.save(auditorium);
        flushAndClear();
        return auditorium;
    }

    protected void insertBlockingScreening(long auditoriumId) {
        OffsetDateTime start = OffsetDateTime.parse("2026-03-01T10:00:00Z");
        OffsetDateTime end = OffsetDateTime.parse("2026-03-01T12:00:00Z");
        OffsetDateTime salesStart = OffsetDateTime.parse("2026-02-20T10:00:00Z");

        jdbcTemplate.update(
                """
                INSERT INTO screening
                (movie_id, auditorium_id, start_time, end_time, sales_start_at, sales_end_at, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                1L,
                auditoriumId,
                java.sql.Timestamp.from(start.toInstant()),
                java.sql.Timestamp.from(end.toInstant()),
                java.sql.Timestamp.from(salesStart.toInstant()),
                java.sql.Timestamp.from(start.toInstant()),
                "SCHEDULED"
        );
    }

    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
