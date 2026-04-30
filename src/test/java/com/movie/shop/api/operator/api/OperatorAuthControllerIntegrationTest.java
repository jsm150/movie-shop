package com.movie.shop.api.operator.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.movie.shop.api.configuration.AbstractContainerBase;
import com.movie.shop.api.operator.domain.aggregate.Operator;
import com.movie.shop.api.operator.domain.aggregate.OperatorRepository;
import com.movie.shop.api.operator.domain.aggregate.permission.OperatorPermission;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;

import tools.jackson.databind.ObjectMapper;

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

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtEncoder jwtEncoder;

    @BeforeEach
    void setUpDefaultOperator() {
        Operator operator = operatorRepository.existsByLoginId(DEFAULT_LOGIN_ID)
                ? operatorRepository.getByLoginId(DEFAULT_LOGIN_ID)
                : Operator.register(
                        DEFAULT_LOGIN_ID,
                        passwordEncoder.encode(DEFAULT_PASSWORD),
                        DEFAULT_DISPLAY_NAME
                );

        grantIfAbsent(operator, new OperatorPermission.MovieManagePermission());
        operatorRepository.save(operator);
    }

    @Test
    @DisplayName("유효한 운영자 계정으로 로그인하면 access token이 발급된다")
    void login_returnsTokenResponse() throws Exception {
        var result = mockMvc.perform(post("/operator/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "admin",
                                  "password": "admin1234"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.operator.loginId").value("admin"))
                .andExpect(jsonPath("$.operator.status").value("ACTIVE"))
                .andReturn();

        assertThat(extractAccessToken(result.getResponse().getContentAsString())).isNotBlank();
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

        assertThat(result.getResponse().getContentAsString()).contains("로그인 ID 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("{noop} 접두사가 있는 평문 비밀번호 계정도 로그인할 수 있다")
    void login_withNoopPassword_returnsOk() throws Exception {
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
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.operator.loginId").value("noop-admin"))
                .andExpect(jsonPath("$.operator.status").value("ACTIVE"))
                .andReturn();

        assertThat(extractAccessToken(result.getResponse().getContentAsString())).isNotBlank();
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

        assertThat(result.getResponse().getContentAsString()).contains("비활성화된 계정입니다.");
    }

    @Test
    @DisplayName("인증 없이 관리 API에 접근하면 401 Unauthorized를 반환한다")
    void managementApi_withoutAuthentication_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/admin/movies")
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
    @DisplayName("로그인한 토큰으로 관리 API 요청을 보내면 정상 처리된다")
    void managementApi_withAuthenticatedToken_returnsSuccess() throws Exception {
        String accessToken = login();

        mockMvc.perform(post("/api/admin/movies")
                        .header("Authorization", bearer(accessToken))
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
    @DisplayName("권한 없는 운영자 토큰으로 관리 API 요청을 보내면 403 Forbidden을 반환한다")
    void managementApi_withUnauthorizedOperatorToken_returnsForbidden() throws Exception {
        operatorRepository.save(Operator.register(
                "no-permission-admin",
                passwordEncoder.encode("admin1234"),
                "No Permission Operator"
        ));

        String accessToken = login("no-permission-admin", "admin1234");

        mockMvc.perform(post("/api/admin/movies")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "덩케르크",
                                  "director": "크리스토퍼 놀란",
                                  "genres": ["전쟁", "드라마"],
                                  "runtimeMinutes": 106,
                                  "audienceRating": "PG12",
                                  "synopsis": "덩케르크 철수 작전",
                                  "releaseDate": "2017-07-20T00:00:00Z",
                                  "casts": [
                                    {
                                      "name": "핀 화이트헤드",
                                      "dateOfBirth": "1997-07-18T00:00:00Z",
                                      "national": "UK",
                                      "role": "토미"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("운영자 권한이 없습니다."));
    }

    @Test
    @DisplayName("로그인한 토큰으로 현재 운영자 정보를 조회할 수 있다")
    void me_withAuthenticatedToken_returnsCurrentOperator() throws Exception {
        String accessToken = login();

        mockMvc.perform(get("/operator/auth/me")
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("admin"))
                .andExpect(jsonPath("$.displayName").value(DEFAULT_DISPLAY_NAME))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("형식이 잘못된 토큰으로 현재 운영자 정보를 조회하면 401 Unauthorized를 반환한다")
    void me_withMalformedToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/operator/auth/me")
                        .header("Authorization", bearer("not-a-jwt")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("서명이 깨진 토큰으로 현재 운영자 정보를 조회하면 401 Unauthorized를 반환한다")
    void me_withInvalidSignatureToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/operator/auth/me")
                        .header("Authorization", bearer(invalidSignatureToken())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("만료된 토큰으로 현재 운영자 정보를 조회하면 401 Unauthorized를 반환한다")
    void me_withExpiredToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/operator/auth/me")
                        .header("Authorization", bearer(expiredToken())))
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

    private String login() throws Exception {
        return login(DEFAULT_LOGIN_ID, DEFAULT_PASSWORD);
    }

    private String login(String loginId, String password) throws Exception {
        var result = mockMvc.perform(post("/operator/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "%s",
                                  "password": "%s"
                                }
                                """.formatted(loginId, password)))
                .andExpect(status().isOk())
                .andReturn();

        return extractAccessToken(result.getResponse().getContentAsString());
    }

    private String extractAccessToken(String responseBody) throws Exception {
        return objectMapper.readTree(responseBody)
                .required("accessToken")
                .asString();
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }

    private void grantIfAbsent(Operator operator, OperatorPermission permission) {
        if (!operator.getPermissions().contains(permission)) {
            operator.grant(permission);
        }
    }

    private String expiredToken() {
        return issueToken(
                Instant.now().minusSeconds(7200),
                Instant.now().minusSeconds(3600),
                jwtEncoder
        );
    }

    private String invalidSignatureToken() {
        SecretKey otherSecretKey = new SecretKeySpec(
                "another-jwt-secret-key-for-invalid-sign".getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        JwtEncoder otherJwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(otherSecretKey));

        return issueToken(
                Instant.now(),
                Instant.now().plusSeconds(3600),
                otherJwtEncoder
        );
    }

    private String issueToken(Instant issuedAt, Instant expiresAt, JwtEncoder tokenEncoder) {
        Operator operator = operatorRepository.getByLoginId(DEFAULT_LOGIN_ID);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(Long.toString(operator.getId()))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("loginId", operator.getLoginId())
                .claim("displayName", operator.getDisplayName())
                .claim("status", operator.getStatus().name())
                .build();

        return tokenEncoder.encode(
                JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(),
                        claims
                )
        ).getTokenValue();
    }
}
