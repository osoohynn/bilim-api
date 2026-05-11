package com.dgsw.bilimapi.commons.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.dgsw.bilimapi.JwtFactory;
import com.dgsw.bilimapi.domain.auth.repository.RefreshTokenRepository;
import com.dgsw.bilimapi.domain.user.domain.UserEntity;
import com.dgsw.bilimapi.domain.user.domain.UserRole;
import com.dgsw.bilimapi.domain.user.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;

@SpringBootTest
class TokenProviderTest {

    @Autowired
    private JwtProvider tokenProvider;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("generateToken: 유저 정보와 만료 기간으로 토큰을 만들 수 있다.")
    @Test
    void generateToken() {
        UserEntity user = userRepository.save(createTestUser("user@bilim.com"));

        String token = tokenProvider.generateToken(user, Duration.ofDays(14));

        Long userId = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecretKey())))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("id", Long.class);

        assertThat(userId).isEqualTo(user.getId());
    }

    @DisplayName("validToken: 만료된 토큰이면 유효성 검증에 실패한다.")
    @Test
    void validToken_expired() {
        String token = JwtFactory.builder()
                .expiration(new Date(new Date().getTime() - Duration.ofDays(7).toMillis()))
                .build()
                .createToken(jwtProperties);

        assertThat(tokenProvider.validToken(token)).isFalse();
    }

    @DisplayName("validToken: 유효한 토큰이면 유효성 검증에 성공한다.")
    @Test
    void validToken_valid() {
        String token = JwtFactory.withDefaultValues().createToken(jwtProperties);

        assertThat(tokenProvider.validToken(token)).isTrue();
    }

    @DisplayName("getAuthentication: 토큰으로 인증 정보를 가져올 수 있다.")
    @Test
    void getAuthentication() {
        String email = "user@bilim.com";
        UserEntity user = userRepository.save(createTestUser(email));

        String token = JwtFactory.builder()
                .subject(email)
                .claims(Map.of("id", user.getId()))
                .build()
                .createToken(jwtProperties);

        Authentication authentication = tokenProvider.getAuthentication(token);

        assertThat(authentication.getName()).isEqualTo(email);
    }

    @DisplayName("getUserId: 토큰으로 유저 ID를 가져올 수 있다.")
    @Test
    void getUserId() {
        Long userId = 999L;
        String token = JwtFactory.builder()
                .claims(Map.of("id", userId))
                .build()
                .createToken(jwtProperties);

        assertThat(tokenProvider.getUserId(token)).isEqualTo(userId);
    }

    private UserEntity createTestUser(String email) {
        return UserEntity.builder()
                .email(email)
                .password("encoded-password")
                .nickname("테스트유저")
                .role(UserRole.USER)
                .lastSeenAt(LocalDateTime.now())
                .build();
    }
}
