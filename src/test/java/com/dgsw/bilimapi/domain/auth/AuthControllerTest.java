package com.dgsw.bilimapi.domain.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dgsw.bilimapi.BaseControllerTest;
import com.dgsw.bilimapi.JwtFactory;
import com.dgsw.bilimapi.commons.security.jwt.JwtProperties;
import com.dgsw.bilimapi.domain.auth.domain.RefreshToken;
import com.dgsw.bilimapi.domain.auth.dto.TokenRefreshRequest;
import com.dgsw.bilimapi.domain.auth.repository.RefreshTokenRepository;
import com.dgsw.bilimapi.domain.user.domain.UserEntity;
import com.dgsw.bilimapi.domain.user.domain.UserRole;
import com.dgsw.bilimapi.domain.auth.dto.LoginRequest;
import com.dgsw.bilimapi.domain.auth.dto.SignupRequest;
import com.dgsw.bilimapi.domain.user.repository.UserRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthControllerTest extends BaseControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("signup: 회원가입에 성공하면 201을 반환한다.")
    @Test
    void signup_success() throws Exception {
        String body = objectMapper.writeValueAsString(signupRequest("new@bilim.com", "password1!", "빌림이"));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @DisplayName("signup: 이미 존재하는 이메일이면 409를 반환한다.")
    @Test
    void signup_duplicateEmail() throws Exception {
        saveUser("dup@bilim.com", "password1!");

        String body = objectMapper.writeValueAsString(signupRequest("dup@bilim.com", "password1!", "빌림이"));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @DisplayName("login: 로그인 성공 시 액세스 토큰과 리프레시 토큰을 반환한다.")
    @Test
    void login_success() throws Exception {
        String rawPassword = "password1!";
        saveUser("user@bilim.com", rawPassword);

        String body = objectMapper.writeValueAsString(loginRequest("user@bilim.com", rawPassword));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @DisplayName("refresh: 유효한 리프레시 토큰으로 새 액세스 토큰을 발급한다.")
    @Test
    void refresh_success() throws Exception {
        UserEntity user = saveUser("user@bilim.com", "password1!");
        String refreshToken = JwtFactory.builder()
                .subject(user.getEmail())
                .claims(Map.of("id", user.getId()))
                .expiration(new java.util.Date(System.currentTimeMillis() + Duration.ofDays(14).toMillis()))
                .build()
                .createToken(jwtProperties);
        refreshTokenRepository.save(new RefreshToken(user.getId(), refreshToken));

        String body = objectMapper.writeValueAsString(new TokenRefreshRequest(refreshToken));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @DisplayName("logout: 유효한 액세스 토큰으로 로그아웃하면 204를 반환하고 리프레시 토큰이 삭제된다.")
    @Test
    void logout_success() throws Exception {
        UserEntity user = saveUser("user@bilim.com", "password1!");
        String accessToken = JwtFactory.builder()
                .subject(user.getEmail())
                .claims(Map.of("id", user.getId()))
                .expiration(new java.util.Date(System.currentTimeMillis() + Duration.ofMinutes(15).toMillis()))
                .build()
                .createToken(jwtProperties);
        String refreshToken = JwtFactory.builder()
                .subject(user.getEmail())
                .claims(Map.of("id", user.getId()))
                .expiration(new java.util.Date(System.currentTimeMillis() + Duration.ofDays(14).toMillis()))
                .build()
                .createToken(jwtProperties);
        refreshTokenRepository.save(new RefreshToken(user.getId(), refreshToken));

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        assertThat(refreshTokenRepository.findByUserId(user.getId())).isEmpty();
    }

    private UserEntity saveUser(String email, String rawPassword) {
        return userRepository.save(UserEntity.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .nickname("테스트유저")
                .role(UserRole.USER)
                .lastSeenAt(LocalDateTime.now())
                .build());
    }

    private SignupRequest signupRequest(String email, String password, String nickname) {
        return new SignupRequest(email, password, nickname);
    }

    private LoginRequest loginRequest(String email, String password) {
        return new LoginRequest(email, password);
    }
}
