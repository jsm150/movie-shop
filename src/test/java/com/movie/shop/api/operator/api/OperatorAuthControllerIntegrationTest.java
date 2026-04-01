package com.movie.shop.api.operator.api;

import static org.assertj.core.api.Assertions.assertThat;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.movie.shop.api.configuration.AbstractContainerBase;
import com.movie.shop.api.operator.domain.aggregate.Operator;
import com.movie.shop.api.operator.domain.aggregate.OperatorRepository;

import jakarta.servlet.http.HttpSession;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OperatorAuthControllerIntegrationTest extends AbstractContainerBase {

    private static final String DEFAULT_LOGIN_ID = "admin";
    private static final String DEFAULT_PASSWORD = "admin1234";
    private static final String DEFAULT_DISPLAY_NAME = "Default Operator";

    @Autowired
    private MockMvc mockMvc;

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
                DEFAULT_DISPLAY_NAME
        ));
    }

    @Test
    @DisplayName("유효한 운영자 계정으로 로그인하면 세션이 생성된다")
    void login_returnsOkAndSession() throws Exception {
        var result = mockMvc.perform(post("/operator/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "admin",
                                  "password": "admin1234"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("admin"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();

        HttpSession session = result.getRequest().getSession(false);
        assertThat(session).isNotNull();
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인하면 401 Unauthorized를 반환한다")
    void login_withInvalidPassword_returnsUnauthorized() throws Exception {
        var result = mockMvc.perform(post("/operator/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "admin",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andReturn();

        assertThat(result.getRequest().getSession(false)).isNull();
    }

    @Test
    @DisplayName("{noop} 접두사가 있는 평문 비밀번호 계정도 로그인할 수 있다")
    void login_withNoopPassword_returnsOkAndSession() throws Exception {
        operatorRepository.save(Operator.register(
                "noop-admin",
                "{noop}admin1234",
                "Noop Operator"
        ));

        var result = mockMvc.perform(post("/operator/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "noop-admin",
                                  "password": "admin1234"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("noop-admin"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();

        assertThat(result.getRequest().getSession(false)).isNotNull();
    }

    @Test
    @DisplayName("비활성 운영자 계정은 로그인할 수 없다")
    void login_withSuspendedOperator_returnsUnauthorized() throws Exception {
        Operator suspendedOperator = Operator.register(
                "suspended-admin",
                passwordEncoder.encode("admin1234"),
                "Suspended Operator"
        );
        suspendedOperator.suspend();
        operatorRepository.save(suspendedOperator);

        var result = mockMvc.perform(post("/operator/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "suspended-admin",
                                  "password": "admin1234"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andReturn();

        assertThat(result.getRequest().getSession(false)).isNull();
    }

    @Test
    @DisplayName("인증 없이 관리 API에 접근하면 401 Unauthorized를 반환한다")
    void managementApi_withoutAuthentication_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "인터스텔라",
                                  "director": "크리스토퍼 놀란",
                                  "genres": ["SF", "드라마"],
                                  "runtimeMinutes": 169,
                                  "audienceRating": "PG12",
                                  "synopsis": "우주 탐사를 통해 인류의 미래를 찾는 이야기",
                                  "releaseDate": "2014-11-07T00:00:00Z",
                                  "casts": [
                                    {
                                      "name": "매튜 매코너히",
                                      "dateOfBirth": "1969-11-04T00:00:00Z",
                                      "national": "USA",
                                      "role": "쿠퍼"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("인증이 필요합니다."));
    }

    @Test
    @DisplayName("로그인한 세션으로 관리 API 요청을 보내면 정상 처리된다")
    void managementApi_withAuthenticatedSession_returnsSuccess() throws Exception {
        MockHttpSession session = login();

        mockMvc.perform(post("/movies")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "오펜하이머",
                                  "director": "크리스토퍼 놀란",
                                  "genres": ["드라마", "전기"],
                                  "runtimeMinutes": 180,
                                  "audienceRating": "PG15",
                                  "synopsis": "핵 개발 프로젝트를 이끈 과학자의 이야기",
                                  "releaseDate": "2023-08-15T00:00:00Z",
                                  "casts": [
                                    {
                                      "name": "킬리언 머피",
                                      "dateOfBirth": "1976-05-25T00:00:00Z",
                                      "national": "Ireland",
                                      "role": "오펜하이머"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("로그인한 세션으로 현재 운영자 정보를 조회할 수 있다")
    void me_withAuthenticatedSession_returnsCurrentOperator() throws Exception {
        MockHttpSession session = login();

        mockMvc.perform(get("/operator/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("admin"))
                .andExpect(jsonPath("$.displayName").value(DEFAULT_DISPLAY_NAME))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("로그아웃 후에는 동일한 세션으로 관리 API에 접근할 수 없다")
    void logout_invalidatesSession() throws Exception {
        MockHttpSession session = login();

        mockMvc.perform(post("/operator/auth/logout").session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/operator/auth/me").session(session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("OpenAPI 문서는 인증 없이 접근 가능하다")
    void apiDocs_withoutAuthentication_areAccessible() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Scalar 문서는 인증 없이 접근 가능하다")
    void scalar_withoutAuthentication_isAccessible() throws Exception {
        mockMvc.perform(get("/scalar"))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isNotEqualTo(401));
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
