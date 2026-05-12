package com.dgsw.bilimapi.domain.friend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dgsw.bilimapi.BaseControllerTest;
import com.dgsw.bilimapi.JwtFactory;
import com.dgsw.bilimapi.commons.security.jwt.JwtProperties;
import com.dgsw.bilimapi.domain.friend.domain.Friendship;
import com.dgsw.bilimapi.domain.friend.domain.FriendshipStatus;
import com.dgsw.bilimapi.domain.friend.repository.FriendshipRepository;
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

class FriendControllerTest extends BaseControllerTest {

    @Autowired private UserRepository userRepository;
    @Autowired private FriendshipRepository friendshipRepository;
    @Autowired private JwtProperties jwtProperties;

    private UserEntity userA;
    private UserEntity userB;
    private String tokenA;
    private String tokenB;

    @BeforeEach
    void setUp() {
        friendshipRepository.deleteAll();
        userRepository.deleteAll();

        userA = saveUser("a@bilim.com", "유저A");
        userB = saveUser("b@bilim.com", "유저B");
        tokenA = createToken(userA);
        tokenB = createToken(userB);
    }

    @DisplayName("sendRequest: 친구 요청에 성공하면 201을 반환한다.")
    @Test
    void sendRequest_success() throws Exception {
        mockMvc.perform(post("/api/friends/request/" + userB.getId())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isCreated());
    }

    @DisplayName("sendRequest: 이미 요청이 존재하면 409를 반환한다.")
    @Test
    void sendRequest_duplicate() throws Exception {
        saveFriendship(userA.getId(), userB.getId(), FriendshipStatus.PENDING);

        mockMvc.perform(post("/api/friends/request/" + userB.getId())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isConflict());
    }

    @DisplayName("sendRequest: 자기 자신에게 요청하면 400을 반환한다.")
    @Test
    void sendRequest_self() throws Exception {
        mockMvc.perform(post("/api/friends/request/" + userA.getId())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("accept: 친구 요청 수락에 성공하면 200을 반환한다.")
    @Test
    void accept_success() throws Exception {
        saveFriendship(userA.getId(), userB.getId(), FriendshipStatus.PENDING);

        mockMvc.perform(post("/api/friends/accept/" + userA.getId())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk());
    }

    @DisplayName("accept: PENDING 요청이 없으면 404를 반환한다.")
    @Test
    void accept_notFound() throws Exception {
        mockMvc.perform(post("/api/friends/accept/" + userA.getId())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }

    @DisplayName("getFriends: 친구 목록을 반환한다.")
    @Test
    void getFriends_success() throws Exception {
        saveFriendship(userA.getId(), userB.getId(), FriendshipStatus.ACCEPTED);

        mockMvc.perform(get("/api/friends")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].friendId").value(userB.getId()))
                .andExpect(jsonPath("$[0].nickname").value("유저B"));
    }

    @DisplayName("getRequests: 받은 친구 요청 목록을 반환한다.")
    @Test
    void getRequests_success() throws Exception {
        saveFriendship(userA.getId(), userB.getId(), FriendshipStatus.PENDING);

        mockMvc.perform(get("/api/friends/requests")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].requesterId").value(userA.getId()))
                .andExpect(jsonPath("$[0].requesterNickname").value("유저A"));
    }

    @DisplayName("removeFriend: 친구 삭제에 성공하면 204를 반환한다.")
    @Test
    void removeFriend_success() throws Exception {
        saveFriendship(userA.getId(), userB.getId(), FriendshipStatus.ACCEPTED);

        mockMvc.perform(delete("/api/friends/" + userB.getId())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());
    }

    @DisplayName("removeFriend: 친구 관계가 없으면 404를 반환한다.")
    @Test
    void removeFriend_notFound() throws Exception {
        mockMvc.perform(delete("/api/friends/" + userB.getId())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());
    }

    private UserEntity saveUser(String email, String nickname) {
        return userRepository.save(UserEntity.builder()
                .email(email)
                .password("encoded")
                .nickname(nickname)
                .role(UserRole.USER)
                .lastSeenAt(LocalDateTime.now())
                .build());
    }

    private void saveFriendship(Long requesterId, Long recipientId, FriendshipStatus status) {
        friendshipRepository.save(Friendship.builder()
                .requesterId(requesterId)
                .recipientId(recipientId)
                .status(status)
                .build());
    }

    private String createToken(UserEntity user) {
        return JwtFactory.builder()
                .subject(user.getEmail())
                .claims(Map.of("id", user.getId()))
                .expiration(new Date(System.currentTimeMillis() + Duration.ofMinutes(15).toMillis()))
                .build()
                .createToken(jwtProperties);
    }
}
