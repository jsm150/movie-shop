package com.movie.shop.api.movie.api;

import com.movie.shop.api.configuration.AbstractContainerBase;
import com.movie.shop.api.movie.api.commands.RegisterMovieCommand;
import com.movie.shop.api.movie.api.commands.RegisterMovieCommand.ActorDto;
import com.movie.shop.api.movie.domain.aggregate.AudienceRating;
import com.movie.shop.api.movie.domain.aggregate.Movie;
import com.movie.shop.api.movie.domain.port.MovieJpaPort;
import com.movie.shop.api.movie.domain.aggregate.MovieRepository;
import com.movie.shop.api.operator.domain.aggregate.Operator;
import com.movie.shop.api.operator.domain.aggregate.OperatorRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MovieControllerIntegrationTest extends AbstractContainerBase {
    private static final String DEFAULT_LOGIN_ID = "admin";
    private static final String DEFAULT_PASSWORD = "admin1234";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private MovieJpaPort movieJpaPort;

    @Autowired
    private OperatorRepository operatorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUpDefaultOperator() {
        if (operatorRepository.existsByLoginId(DEFAULT_LOGIN_ID)) {
            return;
        }

        operatorRepository.save(Operator.register(
                DEFAULT_LOGIN_ID,
                passwordEncoder.encode(DEFAULT_PASSWORD),
                "Default Operator"
        ));
    }

    @Test
    @DisplayName("유효한 영화 등록 요청을 보내면 201 Created와 영화 ID를 반환한다")
    @Transactional
    void registerMovie_returnsCreatedAndId() throws Exception {
        MockHttpSession session = login();

        var command = new RegisterMovieCommand(
                "인터스텔라",
                "크리스토퍼 놀란",
                List.of("SF", "드라마"),
                169,
                AudienceRating.PG12,
                "우주 탐사를 통해 인류의 미래를 찾는 이야기",
                OffsetDateTime.parse("2014-11-07T00:00:00Z"),
                List.of(
                        new ActorDto(
                                "매튜 매코너히",
                                OffsetDateTime.parse("1969-11-04T00:00:00Z"),
                                "USA",
                                "쿠퍼"
                        )
                )
        );

        var result = mockMvc.perform(post("/movies")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(content().string(Matchers.not(Matchers.emptyOrNullString())))
                .andReturn();

        // DB 저장 검증
        Long movieId = Long.parseLong(result.getResponse().getContentAsString());
        Movie savedMovie = movieJpaPort.findById(movieId).orElseThrow();

        assertThat(savedMovie.getTitle().getTitle()).isEqualTo("인터스텔라");
        assertThat(savedMovie.getDirector()).isEqualTo("크리스토퍼 놀란");
        assertThat(savedMovie.getGenres()).containsExactlyInAnyOrder("SF", "드라마");
        assertThat(savedMovie.getRuntimeMinutes()).isEqualTo(169);
        assertThat(savedMovie.getAudienceRating()).isEqualTo(AudienceRating.PG12);
        assertThat(savedMovie.getSynopsis()).isEqualTo("우주 탐사를 통해 인류의 미래를 찾는 이야기");
        assertThat(savedMovie.getReleaseDate()).isEqualTo(OffsetDateTime.parse("2014-11-07T00:00:00Z"));
        assertThat(savedMovie.getCasts()).hasSize(1);
        assertThat(savedMovie.getCasts().getFirst().getName()).isEqualTo("매튜 매코너히");
        assertThat(savedMovie.getCasts().getFirst().getRole()).isEqualTo("쿠퍼");
    }

    @Test
    @DisplayName("제목이 빈 영화 등록 요청을 보내면 400 Bad Request를 반환한다")
    @Transactional
    void registerMovie_withBlankTitle_returnsBadRequest() throws Exception {
        MockHttpSession session = login();

        var command = new RegisterMovieCommand(
                "",  // 빈 제목
                "크리스토퍼 놀란",
                List.of("SF"),
                169,
                AudienceRating.PG12,
                "시놉시스",
                OffsetDateTime.parse("2014-11-07T00:00:00Z"),
                List.of(
                        new ActorDto(
                                "매튜 매코너히",
                                OffsetDateTime.parse("1969-11-04T00:00:00Z"),
                                "USA",
                                "쿠퍼"
                        )
                )
        );

        mockMvc.perform(post("/movies")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());

        // DB에 데이터가 저장되지 않았는지 검증
        assertThat(movieRepository.count()).isZero();
    }

    @Test
    @DisplayName("중복된 제목으로 영화 등록 요청을 보내면 400 Bad Request를 반환한다")
    @Transactional
    void registerMovie_withDuplicateTitle_returnsBadRequest() throws Exception {
        MockHttpSession session = login();

        // 첫 번째 영화 등록
        var firstCommand = new RegisterMovieCommand(
                "다크 나이트",
                "크리스토퍼 놀란",
                List.of("액션", "범죄"),
                152,
                AudienceRating.PG15,
                "배트맨과 조커의 대결",
                OffsetDateTime.parse("2008-08-06T00:00:00Z"),
                List.of(
                        new ActorDto(
                                "크리스찬 베일",
                                OffsetDateTime.parse("1974-01-30T00:00:00Z"),
                                "UK",
                                "브루스 웨인"
                        )
                )
        );

        mockMvc.perform(post("/movies")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstCommand)))
                .andExpect(status().isCreated());

        // 같은 제목으로 두 번째 영화 등록 시도
        var secondCommand = new RegisterMovieCommand(
                "다크 나이트",  // 중복된 제목
                "다른 감독",
                List.of("SF"),
                120,
                AudienceRating.PG12,
                "다른 시놉시스",
                OffsetDateTime.parse("2020-01-01T00:00:00Z"),
                List.of(
                        new ActorDto(
                                "다른 배우",
                                OffsetDateTime.parse("1980-01-01T00:00:00Z"),
                                "USA",
                                "주인공"
                        )
                )
        );

        mockMvc.perform(post("/movies")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondCommand)))
                .andExpect(status().isBadRequest());

        // DB에는 하나만 저장되었는지 검증
        assertThat(movieRepository.count()).isEqualTo(1);
    }

    private MockHttpSession login() throws Exception {
        var result = mockMvc.perform(post("/operator/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "admin",
                                  "password": "admin1234"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        return (MockHttpSession) result.getRequest().getSession(false);
    }
}
