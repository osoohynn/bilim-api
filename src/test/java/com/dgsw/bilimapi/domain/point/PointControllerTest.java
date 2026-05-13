package com.dgsw.bilimapi.domain.point;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dgsw.bilimapi.BaseControllerTest;
import com.dgsw.bilimapi.JwtFactory;
import com.dgsw.bilimapi.commons.security.jwt.JwtProperties;
import com.dgsw.bilimapi.domain.point.domain.UserPoint;
import com.dgsw.bilimapi.domain.point.dto.ChargePointRequest;
import com.dgsw.bilimapi.domain.point.repository.UserPointRepository;
import com.dgsw.bilimapi.domain.user.domain.UserEntity;
import com.dgsw.bilimapi.domain.user.domain.UserRole;
import com.dgsw.bilimapi.domain.user.repository.UserRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class PointControllerTest extends BaseControllerTest {

    @Autowired private UserRepository userRepository;
    @Autowired private UserPointRepository userPointRepository;
    @Autowired private JwtProperties jwtProperties;

    private UserEntity user;
    private String token;

    @BeforeEach
    void setUp() {
        userPointRepository.deleteAll();
        userRepository.deleteAll();
        user = saveUser("user@bilim.com");
        token = createToken(user);
    }

    @DisplayName("getBalance: 포인트 잔액을 반환한다.")
    @Test
    void getBalance() throws Exception {
        userPointRepository.save(new UserPoint(user.getId()));

        mockMvc.perform(get("/api/points")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(0));
    }

    @DisplayName("charge: 포인트를 충전한다.")
    @Test
    void charge() throws Exception {
        String body = objectMapper.writeValueAsString(new ChargePointRequest(1000));

        mockMvc.perform(post("/api/points/charge")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1000));
    }

    private UserEntity saveUser(String email) {
        return userRepository.save(UserEntity.builder()
                .email(email).password("encoded").nickname("유저")
                .role(UserRole.USER).lastSeenAt(LocalDateTime.now()).build());
    }

    private String createToken(UserEntity user) {
        return JwtFactory.builder()
                .subject(user.getEmail())
                .claims(Map.of("id", user.getId()))
                .expiration(new Date(System.currentTimeMillis() + Duration.ofMinutes(15).toMillis()))
                .build().createToken(jwtProperties);
    }
}
