package com.movie.shop.api.movie.domain.aggregate;

import com.movie.shop.api.movie.domain.exceptions.MovieDomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MovieTest {

    @Mock
    private MovieRepository mockRepository;

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

        when(mockRepository.existsByTitle(validTitle)).thenReturn(false);
    }

    @Test
    void createMovie_withValidData_succeeds() {
        // when
        Movie movie = Movie.Register(
                mockRepository,
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
        assertThat(movie.getTitle()).isEqualTo(validTitle);
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
    void createMovie_withBlankTitle_throwsException() {
        assertThatThrownBy(() -> Movie.Register(
                mockRepository,
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
    @MockitoSettings(strictness = Strictness.LENIENT)
    void createMovie_withNullTitle_throwsException() {
        assertThatThrownBy(() -> Movie.Register(
                mockRepository,
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
    void createMovie_withTooLongTitle_throwsException() {
        String longTitle = "a".repeat(201);

        assertThatThrownBy(() -> Movie.Register(
                mockRepository,
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
    void createMovie_withBlankDirector_throwsException() {
        assertThatThrownBy(() -> Movie.Register(
                mockRepository,
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
    void createMovie_withEmptyGenres_throwsException() {
        assertThatThrownBy(() -> Movie.Register(
                mockRepository,
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
    void createMovie_withNullGenres_throwsException() {
        assertThatThrownBy(() -> Movie.Register(
                mockRepository,
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
    void createMovie_withBlankGenreItem_throwsException() {
        assertThatThrownBy(() -> Movie.Register(
                mockRepository,
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
    void createMovie_withZeroRuntimeMinutes_throwsException() {
        assertThatThrownBy(() -> Movie.Register(
                mockRepository,
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
    void createMovie_withNegativeRuntimeMinutes_throwsException() {
        assertThatThrownBy(() -> Movie.Register(
                mockRepository,
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
    void createMovie_withNullAudienceRating_throwsException() {
        assertThatThrownBy(() -> Movie.Register(
                mockRepository,
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
    void createMovie_withBlankSynopsis_throwsException() {
        assertThatThrownBy(() -> Movie.Register(
                mockRepository,
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
    void createMovie_withTooLongSynopsis_throwsException() {
        String longSynopsis = "a".repeat(1001);

        assertThatThrownBy(() -> Movie.Register(
                mockRepository,
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
    void createMovie_withNullReleaseDate_throwsException() {
        assertThatThrownBy(() -> Movie.Register(
                mockRepository,
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
    void createMovie_withEmptyCasts_throwsException() {
        assertThatThrownBy(() -> Movie.Register(
                mockRepository,
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
    void createMovie_withNullCasts_throwsException() {
        assertThatThrownBy(() -> Movie.Register(
                mockRepository,
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
    void createMovie_withDuplicateTitle_throwsException() {
        // given
        when(mockRepository.existsByTitle(validTitle)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> Movie.Register(
                mockRepository,
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
}
