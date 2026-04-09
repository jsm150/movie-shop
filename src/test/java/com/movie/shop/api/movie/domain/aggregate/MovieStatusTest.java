package com.movie.shop.api.movie.domain.aggregate;

import com.movie.shop.api.movie.domain.exceptions.MovieDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MovieStatusTest {

    @Test
    @DisplayName("PREPARING 상태에서 COMING_SOON으로 전이한다")
    void transition_preparingToComingSoon_success() {
        assertThat(MovieStatus.PREPARING.transition(MovieStateChange.COMING_SOON))
                .isEqualTo(MovieStatus.COMING_SOON);
    }

    @Test
    @DisplayName("COMING_SOON 상태에서 NOW_SHOWING으로 전이한다")
    void transition_comingSoonToNowShowing_success() {
        assertThat(MovieStatus.COMING_SOON.transition(MovieStateChange.NOW_SHOWING))
                .isEqualTo(MovieStatus.NOW_SHOWING);
    }

    @Test
    @DisplayName("NOW_SHOWING 상태에서 ENDED로 전이한다")
    void transition_nowShowingToEnded_success() {
        assertThat(MovieStatus.NOW_SHOWING.transition(MovieStateChange.ENDED))
                .isEqualTo(MovieStatus.ENDED);
    }

    @Test
    @DisplayName("PREPARING 상태에서 NOW_SHOWING으로 전이할 수 없다")
    void transition_preparingToNowShowing_throwsException() {
        assertThatThrownBy(() -> MovieStatus.PREPARING.transition(MovieStateChange.NOW_SHOWING))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("COMING_SOON 이 아닌 상태");
    }

    @Test
    @DisplayName("COMING_SOON 상태에서 ENDED로 전이할 수 없다")
    void transition_comingSoonToEnded_throwsException() {
        assertThatThrownBy(() -> MovieStatus.COMING_SOON.transition(MovieStateChange.ENDED))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("NOW_SHOWING 이 아닌 상태");
    }

    @Test
    @DisplayName("NOW_SHOWING 상태에서 COMING_SOON으로 전이할 수 없다")
    void transition_nowShowingToComingSoon_throwsException() {
        assertThatThrownBy(() -> MovieStatus.NOW_SHOWING.transition(MovieStateChange.COMING_SOON))
                .isInstanceOf(MovieDomainException.class)
                .hasMessageContaining("PREPARING 이 아닌 상태");
    }

    @Test
    @DisplayName("ENDED 상태에서는 추가 상태 전이를 할 수 없다")
    void transition_fromEnded_throwsException() {
        assertThatThrownBy(() -> MovieStatus.ENDED.transition(MovieStateChange.ENDED))
                .isInstanceOf(MovieDomainException.class);
    }
}
