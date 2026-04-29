package com.movie.shop.api.movie.domain.aggregate;

import com.movie.shop.api.movie.domain.condition.MovieScreeningPresence;
import com.movie.shop.api.movie.domain.condition.MovieTitleUniquenessCondition;
import com.movie.shop.api.movie.domain.exceptions.MovieDomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MovieTest {

    private MovieTitleUniquenessCondition uniqueTitleCondition;
    private MovieTitleUniquenessCondition duplicateTitleCondition;
    private MovieScreeningPresence emptyScreeningPresence;
    private MovieScreeningPresence linkedScreeningPresence;

    private String validTitle;
    private String validDirector;
    private List<String> validGenres;
    private int validRuntimeMinutes;
    private AudienceRating validAudienceRating;
    private String validSynopsis;
    private OffsetDateTime validReleaseDate;
    private List<Actor> validCasts;

    @BeforeEach
    void setUp() {
        uniqueTitleCondition = new MovieTitleUniquenessCondition(true);
        duplicateTitleCondition = new MovieTitleUniquenessCondition(false);
        emptyScreeningPresence = new MovieScreeningPresence(false);
        linkedScreeningPresence = new MovieScreeningPresence(true);
        validTitle = "인터스텔라";
        validDirector = "크리스토퍼 놀란";
        validGenres = List.of("SF", "드라마");
        validRuntimeMinutes = 169;
        validAudienceRating = AudienceRating.PG12;
        validSynopsis = "우주 탐사를 통해 인류의 미래를 찾는 이야기";
        validReleaseDate = OffsetDateTime.parse("2014-11-07T00:00:00Z");
        validCasts = List.of(
                new Actor("매튜 매코너히", OffsetDateTime.parse("1969-11-04T00:00:00Z"), "USA", "쿠퍼")
        );
    }

    @Test
    @DisplayName("유효한 영화 정보를 생성하면 PREPARING 상태로 생성된다")
    void createMovie_withValidData_succeeds() {
        // when
        Movie movie = Movie.register(
                uniqueTitleCondition,
                validTitle,
                validDirector,
                validGenres,
                validRuntimeMinutes,
                validAudienceRating,
                validSynopsis,
                validReleaseDate,
                validCasts
        );

        // then
        assertThat(movie.getTitle().getTitle()).isEqualTo(validTitle);
        assertThat(movie.getDirector()).isEqualTo(validDirector);
        assertThat(movie.getGenres()).containsExactlyElementsOf(validGenres);
        assertThat(movie.getRuntimeMinutes()).isEqualTo(validRuntimeMinutes);
        assertThat(movie.getAudienceRating()).isEqualTo(validAudienceRating);
        assertThat(movie.getSynopsis()).isEqualTo(validSynopsis);
        assertThat(movie.getReleaseDate()).isEqualTo(validReleaseDate);
        assertThat(movie.getCasts()).hasSize(1);
        assertThat(movie.getStatus()).isEqualTo(MovieStatus.PREPARING);
    }

    @Test
    @DisplayName("영화 제목이 빈 값이면 생성 시 예외가 발생한다")
    void createMovie_withBlankTitle_throwsException() {
        assertThatThrownBy(() -> Movie.register(
                uniqueTitleCondition,
                "",
                validDirector,
                validGenres,
                validRuntimeMinutes,
                validAudienceRating,
                validSynopsis,
                validReleaseDate,
                validCasts
        ))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("영화 제목은 필수입니다");
    }

    @Test
    @DisplayName("영화 제목이 null이면 생성 시 예외가 발생한다")
    void createMovie_withNullTitle_throwsException() {
        assertThatThrownBy(() -> Movie.register(
                uniqueTitleCondition,
                null,
                validDirector,
                validGenres,
                validRuntimeMinutes,
                validAudienceRating,
                validSynopsis,
                validReleaseDate,
                validCasts
        ))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("영화 제목은 필수입니다");
    }

    @Test
    @DisplayName("영화 제목이 200자를 초과하면 생성 시 예외가 발생한다")
    void createMovie_withTooLongTitle_throwsException() {
        String longTitle = "a".repeat(201);

        assertThatThrownBy(() -> Movie.register(
                uniqueTitleCondition,
                longTitle,
                validDirector,
                validGenres,
                validRuntimeMinutes,
                validAudienceRating,
                validSynopsis,
                validReleaseDate,
                validCasts
        ))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("영화 제목은 200자를 초과할 수 없습니다");
    }

    @Test
    @DisplayName("감독 이름이 빈 값이면 생성 시 예외가 발생한다")
    void createMovie_withBlankDirector_throwsException() {
        assertThatThrownBy(() -> Movie.register(
                uniqueTitleCondition,
                validTitle,
                "",
                validGenres,
                validRuntimeMinutes,
                validAudienceRating,
                validSynopsis,
                validReleaseDate,
                validCasts
        ))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("감독 이름은 필수입니다");
    }

    @Test
    @DisplayName("장르 목록이 비어 있으면 생성 시 예외가 발생한다")
    void createMovie_withEmptyGenres_throwsException() {
        assertThatThrownBy(() -> Movie.register(
                uniqueTitleCondition,
                validTitle,
                validDirector,
                Collections.emptyList(),
                validRuntimeMinutes,
                validAudienceRating,
                validSynopsis,
                validReleaseDate,
                validCasts
        ))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("최소 하나 이상의 장르가 필요합니다");
    }

    @Test
    @DisplayName("장르 목록이 null이면 생성 시 예외가 발생한다")
    void createMovie_withNullGenres_throwsException() {
        assertThatThrownBy(() -> Movie.register(
                uniqueTitleCondition,
                validTitle,
                validDirector,
                null,
                validRuntimeMinutes,
                validAudienceRating,
                validSynopsis,
                validReleaseDate,
                validCasts
        ))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("최소 하나 이상의 장르가 필요합니다");
    }

    @Test
    @DisplayName("장르 목록에 빈 값이 있으면 생성 시 예외가 발생한다")
    void createMovie_withBlankGenreItem_throwsException() {
        assertThatThrownBy(() -> Movie.register(
                uniqueTitleCondition,
                validTitle,
                validDirector,
                List.of("SF", ""),
                validRuntimeMinutes,
                validAudienceRating,
                validSynopsis,
                validReleaseDate,
                validCasts
        ))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("장르는 빈 값이나 공백을 포함할 수 없습니다");
    }

    @Test
    @DisplayName("상영 시간이 0이면 생성 시 예외가 발생한다")
    void createMovie_withZeroRuntimeMinutes_throwsException() {
        assertThatThrownBy(() -> Movie.register(
                uniqueTitleCondition,
                validTitle,
                validDirector,
                validGenres,
                0,
                validAudienceRating,
                validSynopsis,
                validReleaseDate,
                validCasts
        ))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("상영 시간은 0보다 커야 합니다");
    }

    @Test
    @DisplayName("상영 시간이 음수면 생성 시 예외가 발생한다")
    void createMovie_withNegativeRuntimeMinutes_throwsException() {
        assertThatThrownBy(() -> Movie.register(
                uniqueTitleCondition,
                validTitle,
                validDirector,
                validGenres,
                -10,
                validAudienceRating,
                validSynopsis,
                validReleaseDate,
                validCasts
        ))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("상영 시간은 0보다 커야 합니다");
    }

    @Test
    @DisplayName("관람 등급이 null이면 생성 시 예외가 발생한다")
    void createMovie_withNullAudienceRating_throwsException() {
        assertThatThrownBy(() -> Movie.register(
                uniqueTitleCondition,
                validTitle,
                validDirector,
                validGenres,
                validRuntimeMinutes,
                null,
                validSynopsis,
                validReleaseDate,
                validCasts
        ))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("유효하지 않은 관람 등급입니다");
    }

    @Test
    @DisplayName("시놉시스가 빈 값이면 생성 시 예외가 발생한다")
    void createMovie_withBlankSynopsis_throwsException() {
        assertThatThrownBy(() -> Movie.register(
                uniqueTitleCondition,
                validTitle,
                validDirector,
                validGenres,
                validRuntimeMinutes,
                validAudienceRating,
                "",
                validReleaseDate,
                validCasts
        ))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("시놉시스는 필수입니다");
    }

    @Test
    @DisplayName("시놉시스가 1000자를 초과하면 생성 시 예외가 발생한다")
    void createMovie_withTooLongSynopsis_throwsException() {
        String longSynopsis = "a".repeat(1001);

        assertThatThrownBy(() -> Movie.register(
                uniqueTitleCondition,
                validTitle,
                validDirector,
                validGenres,
                validRuntimeMinutes,
                validAudienceRating,
                longSynopsis,
                validReleaseDate,
                validCasts
        ))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("시놉시스는 1000자를 초과할 수 없습니다");
    }

    @Test
    @DisplayName("개봉일이 null이면 생성 시 예외가 발생한다")
    void createMovie_withNullReleaseDate_throwsException() {
        assertThatThrownBy(() -> Movie.register(
                uniqueTitleCondition,
                validTitle,
                validDirector,
                validGenres,
                validRuntimeMinutes,
                validAudienceRating,
                validSynopsis,
                null,
                validCasts
        ))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("개봉일은 필수입니다");
    }

    @Test
    @DisplayName("출연진 목록이 비어 있으면 생성 시 예외가 발생한다")
    void createMovie_withEmptyCasts_throwsException() {
        assertThatThrownBy(() -> Movie.register(
                uniqueTitleCondition,
                validTitle,
                validDirector,
                validGenres,
                validRuntimeMinutes,
                validAudienceRating,
                validSynopsis,
                validReleaseDate,
                Collections.emptyList()
        ))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("최소 한 명 이상의 출연진이 필요합니다");
    }

    @Test
    @DisplayName("출연진 목록이 null이면 생성 시 예외가 발생한다")
    void createMovie_withNullCasts_throwsException() {
        assertThatThrownBy(() -> Movie.register(
                uniqueTitleCondition,
                validTitle,
                validDirector,
                validGenres,
                validRuntimeMinutes,
                validAudienceRating,
                validSynopsis,
                validReleaseDate,
                null
        ))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("최소 한 명 이상의 출연진이 필요합니다");
    }

    @Test
    @DisplayName("중복된 제목으로 영화를 생성하면 예외가 발생한다")
    void createMovie_withDuplicateTitle_throwsException() {
        assertThatThrownBy(() -> Movie.register(
                duplicateTitleCondition,
                validTitle,
                validDirector,
                validGenres,
                validRuntimeMinutes,
                validAudienceRating,
                validSynopsis,
                validReleaseDate,
                validCasts
        ))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("이미 존재합니다");
    }

    @Test
    @DisplayName("NOW_SHOWING 상태에서 삭제 가능 여부를 검증하면 예외가 발생한다")
    void validateCanDelete_withNowShowing_throwsException() {
        Movie movie = Movie.register(
                uniqueTitleCondition,
                validTitle,
                validDirector,
                validGenres,
                validRuntimeMinutes,
                validAudienceRating,
                validSynopsis,
                validReleaseDate,
                validCasts
        );
        movie.changeState(MovieStateChange.COMING_SOON);
        movie.changeState(MovieStateChange.NOW_SHOWING);

        assertThatThrownBy(() -> movie.validateCanDelete(emptyScreeningPresence))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("NOW_SHOWING 상태의 영화는 삭제할 수 없습니다.");
    }

    @Test
    @DisplayName("영화 ID가 없으면 삭제 가능 여부 검증 시 예외가 발생한다")
    void validateCanDelete_withoutMovieId_throwsException() {
        Movie movie = Movie.register(
                uniqueTitleCondition,
                validTitle,
                validDirector,
                validGenres,
                validRuntimeMinutes,
                validAudienceRating,
                validSynopsis,
                validReleaseDate,
                validCasts
        );

        assertThatThrownBy(() -> movie.validateCanDelete(emptyScreeningPresence))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("영화 ID가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("상영이 연결된 영화는 삭제할 수 없다")
    void validateCanDelete_withLinkedScreening_throwsException() throws Exception {
        Movie movie = createMovieWithId(1L);

        assertThatThrownBy(() -> movie.validateCanDelete(linkedScreeningPresence))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("상영이 연결된 영화는 삭제할 수 없습니다.");
    }

    @Test
    @DisplayName("PREPARING 상태이고 상영이 연결되지 않으면 삭제 검증에 성공한다")
    void validateCanDelete_withPreparingAndNoScreening_doesNotThrow() throws Exception {
        Movie movie = createMovieWithId(1L);

        assertThatCode(() -> movie.validateCanDelete(emptyScreeningPresence))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("PREPARING 상태이면 상영 가능 여부가 false를 반환한다")
    void canBeScheduled_withPreparing_returnsFalse() {
        Movie movie = Movie.register(
                uniqueTitleCondition,
                validTitle,
                validDirector,
                validGenres,
                validRuntimeMinutes,
                validAudienceRating,
                validSynopsis,
                validReleaseDate,
                validCasts
        );

        assertThat(movie.canBeScheduled()).isFalse();
    }

    @Test
    @DisplayName("COMING_SOON 상태이면 상영 가능 여부가 true를 반환한다")
    void canBeScheduled_withComingSoon_returnsTrue() {
        Movie movie = Movie.register(
                uniqueTitleCondition,
                validTitle,
                validDirector,
                validGenres,
                validRuntimeMinutes,
                validAudienceRating,
                validSynopsis,
                validReleaseDate,
                validCasts
        );
        movie.changeState(MovieStateChange.COMING_SOON);

        assertThat(movie.canBeScheduled()).isTrue();
    }

    @Test
    @DisplayName("NOW_SHOWING 상태이면 상영 가능 여부가 true를 반환한다")
    void canBeScheduled_withNowShowing_returnsTrue() {
        Movie movie = Movie.register(
                uniqueTitleCondition,
                validTitle,
                validDirector,
                validGenres,
                validRuntimeMinutes,
                validAudienceRating,
                validSynopsis,
                validReleaseDate,
                validCasts
        );
        movie.changeState(MovieStateChange.COMING_SOON);
        movie.changeState(MovieStateChange.NOW_SHOWING);

        assertThat(movie.canBeScheduled()).isTrue();
    }

    @Test
    @DisplayName("ENDED 상태이면 상영 가능 여부가 false를 반환한다")
    void canBeScheduled_withEnded_returnsFalse() {
        Movie movie = Movie.register(
                uniqueTitleCondition,
                validTitle,
                validDirector,
                validGenres,
                validRuntimeMinutes,
                validAudienceRating,
                validSynopsis,
                validReleaseDate,
                validCasts
        );
        movie.changeState(MovieStateChange.COMING_SOON);
        movie.changeState(MovieStateChange.NOW_SHOWING);
        movie.changeState(MovieStateChange.ENDED);

        assertThat(movie.canBeScheduled()).isFalse();
    }

    private Movie createMovieWithId(long movieId) throws Exception {
        Movie movie = Movie.register(
                uniqueTitleCondition,
                validTitle,
                validDirector,
                validGenres,
                validRuntimeMinutes,
                validAudienceRating,
                validSynopsis,
                validReleaseDate,
                validCasts
        );

        Field idField = Movie.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(movie, movieId);
        return movie;
    }
}
