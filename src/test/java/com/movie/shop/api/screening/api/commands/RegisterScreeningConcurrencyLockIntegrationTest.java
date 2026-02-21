package com.movie.shop.api.screening.api.commands;

import an.awesome.pipelinr.Pipeline;
import com.movie.shop.api.configuration.AbstractContainerBase;
import com.movie.shop.api.movie.api.commands.ChangeStateMovieCommand;
import com.movie.shop.api.movie.api.commands.RegisterMovieCommand;
import com.movie.shop.api.movie.domain.aggregate.AudienceRating;
import com.movie.shop.api.movie.domain.aggregate.MovieStateChange;
import com.movie.shop.api.screening.domain.aggregate.Screening;
import com.movie.shop.api.screening.domain.exceptions.ScreeningDomainException;
import com.movie.shop.api.screening.domain.port.ScreeningJpaPort;
import com.movie.shop.api.theater.api.commands.RegisterTheaterCommand;
import com.movie.shop.api.theater.domain.aggregate.TheaterType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("상영 동시성 락 통합 테스트")
class RegisterScreeningConcurrencyLockIntegrationTest extends AbstractContainerBase {

    private static final AtomicLong SEQUENCE = new AtomicLong(1L);

    @Autowired
    private Pipeline pipeline;

    @Autowired
    private ScreeningJpaPort screeningJpaPort;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        cleanupCommittedData();
    }

    @AfterEach
    void tearDown() {
        cleanupCommittedData();
    }

    @Test
    @DisplayName("등록 경합 시 갭락으로 두 번째 트랜잭션은 대기 후 충돌 예외가 발생한다")
    void registerScreening_concurrently_gapLockBlocksAndRejectsSecond() throws Exception {
        long movieId = registerSchedulableMovie();
        long theaterId = registerTheater();

        RegisterScreeningCommand firstCommand = new RegisterScreeningCommand(
                movieId,
                theaterId,
                OffsetDateTime.parse("2026-03-01T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T12:00:00Z"),
                OffsetDateTime.parse("2026-02-20T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T10:00:00Z")
        );
        RegisterScreeningCommand secondCommand = new RegisterScreeningCommand(
                movieId,
                theaterId,
                OffsetDateTime.parse("2026-03-01T11:00:00Z"),
                OffsetDateTime.parse("2026-03-01T13:00:00Z"),
                OffsetDateTime.parse("2026-02-20T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T11:00:00Z")
        );

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch firstTxReady = new CountDownLatch(1);
        CountDownLatch releaseFirstTx = new CountDownLatch(1);

        try {
            Future<Long> firstFuture = executor.submit(() ->
                    transactionTemplate.execute(status -> {
                        Long screeningId = pipeline.send(firstCommand);
                        firstTxReady.countDown();
                        awaitLatch(releaseFirstTx, "첫 번째 트랜잭션 해제 대기 실패");
                        return screeningId;
                    })
            );

            awaitLatch(firstTxReady, "첫 번째 트랜잭션 준비 대기 실패");

            Future<Long> secondFuture = executor.submit(() -> pipeline.send(secondCommand));

            Thread.sleep(300L);
            assertThat(secondFuture.isDone()).isFalse();

            releaseFirstTx.countDown();

            Long firstScreeningId = firstFuture.get(10, TimeUnit.SECONDS);
            assertThat(firstScreeningId).isNotNull();

            Throwable secondFailure = extractFutureFailure(secondFuture);
            assertThat(secondFailure).isInstanceOf(ScreeningDomainException.class)
                    .hasMessageContaining("동일한 극장에 상영 시간이 겹치는 일정이 존재합니다.");

            List<Screening> screenings = screeningJpaPort.findAllByTheaterId(theaterId);
            assertThat(screenings).hasSize(1);
            assertThat(screenings.getFirst().getId()).isEqualTo(firstScreeningId);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("수정 경합 시 self-exclude 락으로 두 번째 수정이 대기 후 진행된다")
    void updateScreening_concurrently_selfExcludeLockWaitsThenProceeds() throws Exception {
        long movieId = registerSchedulableMovie();
        long theaterId = registerTheater();

        long secondScreeningId = registerScreening(
                movieId,
                theaterId,
                OffsetDateTime.parse("2026-03-01T16:00:00Z"),
                OffsetDateTime.parse("2026-03-01T18:00:00Z")
        );

        UpdateScreeningCommand secondUpdate = new UpdateScreeningCommand(
                secondScreeningId,
                OffsetDateTime.parse("2026-03-01T12:00:00Z"),
                OffsetDateTime.parse("2026-03-01T14:00:00Z"),
                OffsetDateTime.parse("2026-02-20T10:00:00Z"),
                OffsetDateTime.parse("2026-03-01T12:00:00Z")
        );

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch firstTxReady = new CountDownLatch(1);
        CountDownLatch releaseFirstTx = new CountDownLatch(1);

        try {
            Future<Void> firstFuture = executor.submit(() ->
                    transactionTemplate.execute(status -> {
                        screeningJpaPort.findConflictCandidatesByTheaterIdAndIdNot(
                                theaterId,
                                secondUpdate.screeningStartTime(),
                                secondUpdate.screeningEndTime(),
                                secondScreeningId
                        );
                        firstTxReady.countDown();
                        awaitLatch(releaseFirstTx, "첫 번째 트랜잭션 해제 대기 실패");
                        return null;
                    })
            );

            awaitLatch(firstTxReady, "첫 번째 트랜잭션 준비 대기 실패");

            Future<Long> secondFuture = executor.submit(() -> pipeline.send(secondUpdate));

            Thread.sleep(300L);
            assertThat(secondFuture.isDone()).isFalse();

            releaseFirstTx.countDown();

            firstFuture.get(10, TimeUnit.SECONDS);
            Long updatedId = secondFuture.get(10, TimeUnit.SECONDS);
            assertThat(updatedId).isEqualTo(secondScreeningId);

            Screening secondScreening = screeningJpaPort.findById(secondScreeningId).orElseThrow();

            assertThat(secondScreening.getScreeningTimeRange().getStartTime())
                    .isEqualTo(OffsetDateTime.parse("2026-03-01T12:00:00Z"));
            assertThat(secondScreening.getScreeningTimeRange().getEndTime())
                    .isEqualTo(OffsetDateTime.parse("2026-03-01T14:00:00Z"));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("동시 insert 경합에서 gap lock 구간 조회는 공존하지만 insert는 1건만 성공한다")
    void gapLock_allowsConcurrentRangeLocks_butSimultaneousInsertEndsWithSingleWinner() throws Exception {
        long movieId = registerSchedulableMovie();
        long theaterId = registerTheater();

        OffsetDateTime firstStart = OffsetDateTime.parse("2026-03-02T10:00:00Z");
        OffsetDateTime firstEnd = OffsetDateTime.parse("2026-03-02T12:00:00Z");
        OffsetDateTime secondStart = OffsetDateTime.parse("2026-03-02T11:00:00Z");
        OffsetDateTime secondEnd = OffsetDateTime.parse("2026-03-02T13:00:00Z");

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CyclicBarrier lockBarrier = new CyclicBarrier(2);
        CyclicBarrier insertBarrier = new CyclicBarrier(2);
        AtomicInteger lockStagePassed = new AtomicInteger(0);

        try {
            Future<Throwable> firstFuture = executor.submit(() -> runConcurrentInsertTransaction(
                    movieId,
                    theaterId,
                    firstStart,
                    firstEnd,
                    firstStart,
                    firstEnd,
                    lockBarrier,
                    insertBarrier,
                    lockStagePassed
            ));
            Future<Throwable> secondFuture = executor.submit(() -> runConcurrentInsertTransaction(
                    movieId,
                    theaterId,
                    secondStart,
                    secondEnd,
                    secondStart,
                    secondEnd,
                    lockBarrier,
                    insertBarrier,
                    lockStagePassed
            ));

            Throwable firstFailure = firstFuture.get(10, TimeUnit.SECONDS);
            Throwable secondFailure = secondFuture.get(10, TimeUnit.SECONDS);

            assertThat(lockStagePassed.get()).isEqualTo(2);

            int successCount = (firstFailure == null ? 1 : 0) + (secondFailure == null ? 1 : 0);
            assertThat(successCount).isEqualTo(1);

            Throwable failure = firstFailure != null ? firstFailure : secondFailure;
            assertThat(failure).isNotNull();
            assertThat(isLockConflictFailure(failure)).isTrue();

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM screening WHERE theater_id = ?",
                    Integer.class,
                    theaterId
            );
            assertThat(count).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }

    private long registerSchedulableMovie() {
        long seq = SEQUENCE.getAndIncrement();
        Long movieId = pipeline.send(new RegisterMovieCommand(
                "동시성영화-" + seq,
                "동시성감독-" + seq,
                List.of("드라마"),
                120,
                AudienceRating.PG12,
                "동시성 검증 영화",
                OffsetDateTime.parse("2026-01-01T00:00:00Z"),
                List.of(new RegisterMovieCommand.ActorDto(
                        "동시성배우-" + seq,
                        OffsetDateTime.parse("1990-01-01T00:00:00Z"),
                        "Korea",
                        "주연"
                ))
        ));

        pipeline.send(new ChangeStateMovieCommand(movieId, MovieStateChange.COMING_SOON));
        return movieId;
    }

    private long registerTheater() {
        long seq = SEQUENCE.getAndIncrement();
        return pipeline.send(new RegisterTheaterCommand(
                "동시성극장-" + seq,
                1,
                TheaterType.Standard,
                List.of("A1", "A2", "B1", "B2"),
                2,
                2
        ));
    }

    private long registerScreening(long movieId, long theaterId, OffsetDateTime start, OffsetDateTime end) {
        return pipeline.send(new RegisterScreeningCommand(
                movieId,
                theaterId,
                start,
                end,
                OffsetDateTime.parse("2026-02-20T10:00:00Z"),
                start
        ));
    }

    private Throwable runConcurrentInsertTransaction(long movieId,
                                                     long theaterId,
                                                     OffsetDateTime lockQueryStart,
                                                     OffsetDateTime lockQueryEnd,
                                                     OffsetDateTime insertStart,
                                                     OffsetDateTime insertEnd,
                                                     CyclicBarrier lockBarrier,
                                                     CyclicBarrier insertBarrier,
                                                     AtomicInteger lockStagePassed) {
        try {
            transactionTemplate.execute(status -> {
                withTemporaryLockWaitTimeout(3, () -> {
                    screeningJpaPort.findConflictCandidatesByTheaterId(
                            theaterId,
                            lockQueryStart,
                            lockQueryEnd
                    );

                    awaitBarrier(lockBarrier, "gap lock 구간 동기화 실패");
                    lockStagePassed.incrementAndGet();
                    awaitBarrier(insertBarrier, "동시 insert 동기화 실패");

                    jdbcTemplate.update(
                            """
                            INSERT INTO screening
                            (movie_id, theater_id, start_time, end_time, sales_start_at, sales_end_at, status)
                            VALUES (?, ?, ?, ?, ?, ?, ?)
                            """,
                            movieId,
                            theaterId,
                            java.sql.Timestamp.from(insertStart.toInstant()),
                            java.sql.Timestamp.from(insertEnd.toInstant()),
                            java.sql.Timestamp.from(insertStart.minusDays(3).toInstant()),
                            java.sql.Timestamp.from(insertStart.toInstant()),
                            "SCHEDULED"
                    );
                });

                return null;
            });
            return null;
        } catch (Exception e) {
            return unwrapRootCause(e);
        }
    }

    private int readSessionLockWaitTimeout() {
        Integer timeout = jdbcTemplate.queryForObject(
                "SELECT @@SESSION.innodb_lock_wait_timeout",
                Integer.class
        );
        if (timeout == null) {
            throw new IllegalStateException("세션 lock wait timeout 값을 조회할 수 없습니다.");
        }
        return timeout;
    }

    private void setSessionLockWaitTimeout(int seconds) {
        jdbcTemplate.update("SET SESSION innodb_lock_wait_timeout = ?", seconds);
    }

    private void withTemporaryLockWaitTimeout(int temporarySeconds, Runnable action) {
        int originalTimeout = readSessionLockWaitTimeout();
        setSessionLockWaitTimeout(temporarySeconds);

        RuntimeException runtimeFailure = null;
        Error errorFailure = null;
        try {
            action.run();
        } catch (RuntimeException e) {
            runtimeFailure = e;
            throw e;
        } catch (Error e) {
            errorFailure = e;
            throw e;
        } finally {
            try {
                setSessionLockWaitTimeout(originalTimeout);
            } catch (RuntimeException restoreFailure) {
                IllegalStateException restoreException =
                        new IllegalStateException("세션 변수 복원 실패", restoreFailure);
                if (runtimeFailure != null) {
                    restoreException.addSuppressed(runtimeFailure);
                }
                if (errorFailure != null) {
                    restoreException.addSuppressed(errorFailure);
                }
                throw restoreException;
            }
        }
    }

    private void cleanupCommittedData() {
        jdbcTemplate.update("DELETE FROM screening");
        jdbcTemplate.update("DELETE FROM movie_actors");
        jdbcTemplate.update("DELETE FROM movie_genres");
        jdbcTemplate.update("DELETE FROM movie");
        jdbcTemplate.update("DELETE FROM theater");
    }

    private void awaitLatch(CountDownLatch latch, String failureMessage) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException(failureMessage);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(failureMessage, e);
        }
    }

    private Throwable extractFutureFailure(Future<?> future) {
        try {
            future.get(10, TimeUnit.SECONDS);
            throw new IllegalStateException("예외가 발생해야 하지만 성공했습니다.");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            while (cause != null && cause.getCause() != null) {
                cause = cause.getCause();
            }
            return cause;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Future 대기 중 인터럽트가 발생했습니다.", e);
        } catch (TimeoutException e) {
            throw new IllegalStateException("Future 결과 대기 시간이 초과되었습니다.", e);
        }
    }

    private void awaitBarrier(CyclicBarrier barrier, String failureMessage) {
        try {
            barrier.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(failureMessage, e);
        } catch (TimeoutException | BrokenBarrierException e) {
            throw new IllegalStateException(failureMessage, e);
        }
    }

    private boolean isLockConflictFailure(Throwable failure) {
        Throwable cause = unwrapRootCause(failure);
        String message = cause.getMessage();
        if (message == null) {
            return false;
        }

        return message.contains("1213")
                || message.contains("1205")
                || message.contains("Deadlock found")
                || message.contains("Lock wait timeout exceeded");
    }

    private Throwable unwrapRootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current != null && current.getCause() != null) {
            current = current.getCause();
        }
        return current == null ? throwable : current;
    }

    private int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }
}
