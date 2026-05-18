package com.dgsw.bilimapi.domain.reading;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dgsw.bilimapi.BaseControllerTest;
import com.dgsw.bilimapi.JwtFactory;
import com.dgsw.bilimapi.commons.security.jwt.JwtProperties;
import com.dgsw.bilimapi.domain.book.domain.Book;
import com.dgsw.bilimapi.domain.book.domain.UserBook;
import com.dgsw.bilimapi.domain.book.repository.BookRepository;
import com.dgsw.bilimapi.domain.book.repository.UserBookRepository;
import com.dgsw.bilimapi.domain.friend.domain.Friendship;
import com.dgsw.bilimapi.domain.friend.domain.FriendshipStatus;
import com.dgsw.bilimapi.domain.friend.repository.FriendshipRepository;
import com.dgsw.bilimapi.domain.reading.domain.InvitationStatus;
import com.dgsw.bilimapi.domain.reading.domain.ReadingSession;
import com.dgsw.bilimapi.domain.reading.domain.ReadingSessionStatus;
import com.dgsw.bilimapi.domain.reading.domain.SessionInvitation;
import com.dgsw.bilimapi.domain.reading.domain.SessionMessage;
import com.dgsw.bilimapi.domain.reading.domain.SessionParticipant;
import com.dgsw.bilimapi.domain.reading.repository.ReadingSessionRepository;
import com.dgsw.bilimapi.domain.reading.repository.SessionInvitationRepository;
import com.dgsw.bilimapi.domain.reading.repository.SessionMessageRepository;
import com.dgsw.bilimapi.domain.reading.repository.SessionParticipantRepository;
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

class ReadingSessionControllerTest extends BaseControllerTest {

    @Autowired private UserRepository userRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private UserBookRepository userBookRepository;
    @Autowired private FriendshipRepository friendshipRepository;
    @Autowired private ReadingSessionRepository sessionRepository;
    @Autowired private SessionParticipantRepository participantRepository;
    @Autowired private SessionInvitationRepository invitationRepository;
    @Autowired private SessionMessageRepository messageRepository;
    @Autowired private JwtProperties jwtProperties;

    private UserEntity host;
    private UserEntity friend;
    private UserEntity stranger;
    private Book book;
    private String tokenHost;
    private String tokenFriend;
    private String tokenStranger;

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
        invitationRepository.deleteAll();
        participantRepository.deleteAll();
        sessionRepository.deleteAll();
        userBookRepository.deleteAll();
        friendshipRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();

        host = saveUser("host@bilim.com", "호스트");
        friend = saveUser("friend@bilim.com", "친구");
        stranger = saveUser("stranger@bilim.com", "모르는사람");
        book = saveBook("테스트책");
        saveUserBook(host.getId(), book.getId());
        saveFriendship(host.getId(), friend.getId(), FriendshipStatus.ACCEPTED);

        tokenHost = createToken(host);
        tokenFriend = createToken(friend);
        tokenStranger = createToken(stranger);
    }

    @DisplayName("create: 소유한 책으로 세션을 생성하면 201을 반환한다.")
    @Test
    void create_success() throws Exception {
        mockMvc.perform(post("/api/reading-sessions")
                        .header("Authorization", "Bearer " + tokenHost)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("bookId", book.getId()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.hostId").value(host.getId()));
    }

    @DisplayName("create: 소유하지 않은 책으로 세션 생성 시 403을 반환한다.")
    @Test
    void create_bookNotOwned() throws Exception {
        mockMvc.perform(post("/api/reading-sessions")
                        .header("Authorization", "Bearer " + tokenStranger)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("bookId", book.getId()))))
                .andExpect(status().isForbidden());
    }

    @DisplayName("getMySessions: 참가 중인 세션 목록을 반환한다.")
    @Test
    void getMySessions_success() throws Exception {
        ReadingSession session = saveSession(host.getId(), book.getId(), ReadingSessionStatus.WAITING);
        saveParticipant(session.getId(), host.getId());

        mockMvc.perform(get("/api/reading-sessions")
                        .header("Authorization", "Bearer " + tokenHost))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].hostId").value(host.getId()));
    }

    @DisplayName("getDetail: 참가자가 세션 상세를 조회하면 200을 반환한다.")
    @Test
    void getDetail_asParticipant() throws Exception {
        ReadingSession session = saveSession(host.getId(), book.getId(), ReadingSessionStatus.WAITING);
        saveParticipant(session.getId(), host.getId());

        mockMvc.perform(get("/api/reading-sessions/" + session.getId())
                        .header("Authorization", "Bearer " + tokenHost))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hostId").value(host.getId()))
                .andExpect(jsonPath("$.bookTitle").value("테스트책"));
    }

    @DisplayName("getDetail: 비참가자가 조회하면 403을 반환한다.")
    @Test
    void getDetail_notParticipant() throws Exception {
        ReadingSession session = saveSession(host.getId(), book.getId(), ReadingSessionStatus.WAITING);
        saveParticipant(session.getId(), host.getId());

        mockMvc.perform(get("/api/reading-sessions/" + session.getId())
                        .header("Authorization", "Bearer " + tokenStranger))
                .andExpect(status().isForbidden());
    }

    @DisplayName("invite: 호스트가 친구를 초대하면 200을 반환한다.")
    @Test
    void invite_success() throws Exception {
        ReadingSession session = saveSession(host.getId(), book.getId(), ReadingSessionStatus.WAITING);
        saveParticipant(session.getId(), host.getId());

        mockMvc.perform(post("/api/reading-sessions/" + session.getId() + "/invite/" + friend.getId())
                        .header("Authorization", "Bearer " + tokenHost))
                .andExpect(status().isOk());
    }

    @DisplayName("invite: 친구가 아닌 사람을 초대하면 400을 반환한다.")
    @Test
    void invite_notFriends() throws Exception {
        ReadingSession session = saveSession(host.getId(), book.getId(), ReadingSessionStatus.WAITING);
        saveParticipant(session.getId(), host.getId());

        mockMvc.perform(post("/api/reading-sessions/" + session.getId() + "/invite/" + stranger.getId())
                        .header("Authorization", "Bearer " + tokenHost))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("invite: 호스트가 아닌 사람이 초대하면 403을 반환한다.")
    @Test
    void invite_notHost() throws Exception {
        ReadingSession session = saveSession(host.getId(), book.getId(), ReadingSessionStatus.ACTIVE);
        saveParticipant(session.getId(), host.getId());
        saveParticipant(session.getId(), friend.getId());

        mockMvc.perform(post("/api/reading-sessions/" + session.getId() + "/invite/" + stranger.getId())
                        .header("Authorization", "Bearer " + tokenFriend))
                .andExpect(status().isForbidden());
    }

    @DisplayName("invite: 세션이 4인 꽉 찼을 때 초대하면 409를 반환한다.")
    @Test
    void invite_sessionFull() throws Exception {
        ReadingSession session = saveSession(host.getId(), book.getId(), ReadingSessionStatus.ACTIVE);
        saveParticipant(session.getId(), host.getId());
        saveParticipant(session.getId(), saveUser("u2@bilim.com", "유저2").getId());
        saveParticipant(session.getId(), saveUser("u3@bilim.com", "유저3").getId());
        saveParticipant(session.getId(), saveUser("u4@bilim.com", "유저4").getId());

        mockMvc.perform(post("/api/reading-sessions/" + session.getId() + "/invite/" + friend.getId())
                        .header("Authorization", "Bearer " + tokenHost))
                .andExpect(status().isConflict());
    }

    @DisplayName("getInvitations: 받은 초대 목록을 반환한다.")
    @Test
    void getInvitations_success() throws Exception {
        ReadingSession session = saveSession(host.getId(), book.getId(), ReadingSessionStatus.WAITING);
        saveParticipant(session.getId(), host.getId());
        saveInvitation(session.getId(), friend.getId(), InvitationStatus.PENDING);

        mockMvc.perform(get("/api/reading-sessions/invitations")
                        .header("Authorization", "Bearer " + tokenFriend))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].bookTitle").value("테스트책"));
    }

    @DisplayName("acceptInvitation: 초대를 수락하면 200을 반환하고 세션에 참가된다.")
    @Test
    void acceptInvitation_success() throws Exception {
        ReadingSession session = saveSession(host.getId(), book.getId(), ReadingSessionStatus.WAITING);
        saveParticipant(session.getId(), host.getId());
        SessionInvitation invitation = saveInvitation(session.getId(), friend.getId(), InvitationStatus.PENDING);

        mockMvc.perform(post("/api/reading-sessions/invitations/" + invitation.getId() + "/accept")
                        .header("Authorization", "Bearer " + tokenFriend))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participants.length()").value(2));
    }

    @DisplayName("rejectInvitation: 초대를 거절하면 200을 반환한다.")
    @Test
    void rejectInvitation_success() throws Exception {
        ReadingSession session = saveSession(host.getId(), book.getId(), ReadingSessionStatus.WAITING);
        saveParticipant(session.getId(), host.getId());
        SessionInvitation invitation = saveInvitation(session.getId(), friend.getId(), InvitationStatus.PENDING);

        mockMvc.perform(post("/api/reading-sessions/invitations/" + invitation.getId() + "/reject")
                        .header("Authorization", "Bearer " + tokenFriend))
                .andExpect(status().isOk());
    }

    @DisplayName("leaveSession: 참가자가 세션을 나가면 200을 반환한다.")
    @Test
    void leaveSession_asParticipant() throws Exception {
        ReadingSession session = saveSession(host.getId(), book.getId(), ReadingSessionStatus.ACTIVE);
        saveParticipant(session.getId(), host.getId());
        saveParticipant(session.getId(), friend.getId());

        mockMvc.perform(post("/api/reading-sessions/" + session.getId() + "/leave")
                        .header("Authorization", "Bearer " + tokenFriend))
                .andExpect(status().isOk());
    }

    @DisplayName("leaveSession: 호스트가 나가면 세션이 ENDED된다.")
    @Test
    void leaveSession_hostEndsSession() throws Exception {
        ReadingSession session = saveSession(host.getId(), book.getId(), ReadingSessionStatus.WAITING);
        saveParticipant(session.getId(), host.getId());

        mockMvc.perform(post("/api/reading-sessions/" + session.getId() + "/leave")
                        .header("Authorization", "Bearer " + tokenHost))
                .andExpect(status().isOk());
    }

    @DisplayName("endSession: 호스트가 세션을 종료하면 204를 반환한다.")
    @Test
    void endSession_success() throws Exception {
        ReadingSession session = saveSession(host.getId(), book.getId(), ReadingSessionStatus.ACTIVE);
        saveParticipant(session.getId(), host.getId());

        mockMvc.perform(delete("/api/reading-sessions/" + session.getId())
                        .header("Authorization", "Bearer " + tokenHost))
                .andExpect(status().isNoContent());
    }

    @DisplayName("endSession: 호스트가 아닌 사람이 종료하면 403을 반환한다.")
    @Test
    void endSession_notHost() throws Exception {
        ReadingSession session = saveSession(host.getId(), book.getId(), ReadingSessionStatus.ACTIVE);
        saveParticipant(session.getId(), host.getId());
        saveParticipant(session.getId(), friend.getId());

        mockMvc.perform(delete("/api/reading-sessions/" + session.getId())
                        .header("Authorization", "Bearer " + tokenFriend))
                .andExpect(status().isForbidden());
    }

    @DisplayName("getMessages: 참가자가 채팅 히스토리를 조회하면 200을 반환한다.")
    @Test
    void getMessages_success() throws Exception {
        ReadingSession session = saveSession(host.getId(), book.getId(), ReadingSessionStatus.ACTIVE);
        saveParticipant(session.getId(), host.getId());
        saveMessage(session.getId(), host.getId(), "안녕하세요");

        mockMvc.perform(get("/api/reading-sessions/" + session.getId() + "/messages")
                        .header("Authorization", "Bearer " + tokenHost))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].content").value("안녕하세요"));
    }

    @DisplayName("getEpubUrl: 참가자가 epub URL을 조회하면 200을 반환한다.")
    @Test
    void getEpubUrl_asParticipant() throws Exception {
        ReadingSession session = saveSession(host.getId(), book.getId(), ReadingSessionStatus.ACTIVE);
        saveParticipant(session.getId(), host.getId());

        mockMvc.perform(get("/api/reading-sessions/" + session.getId() + "/epub")
                        .header("Authorization", "Bearer " + tokenHost))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").exists());
    }

    @DisplayName("getEpubUrl: 비참가자가 epub URL을 조회하면 403을 반환한다.")
    @Test
    void getEpubUrl_notParticipant() throws Exception {
        ReadingSession session = saveSession(host.getId(), book.getId(), ReadingSessionStatus.ACTIVE);
        saveParticipant(session.getId(), host.getId());

        mockMvc.perform(get("/api/reading-sessions/" + session.getId() + "/epub")
                        .header("Authorization", "Bearer " + tokenStranger))
                .andExpect(status().isForbidden());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private UserEntity saveUser(String email, String nickname) {
        return userRepository.save(UserEntity.builder()
                .email(email)
                .password("encoded")
                .nickname(nickname)
                .role(UserRole.USER)
                .lastSeenAt(LocalDateTime.now())
                .build());
    }

    private Book saveBook(String title) {
        return bookRepository.save(Book.builder()
                .title(title)
                .author("저자")
                .price(10000)
                .contentUrl("https://example.com/test.epub")
                .build());
    }

    private void saveUserBook(Long ownerId, Long bookId) {
        userBookRepository.save(UserBook.builder()
                .bookId(bookId)
                .ownerId(ownerId)
                .holderId(ownerId)
                .isPublic(false)
                .build());
    }

    private void saveFriendship(Long requesterId, Long recipientId, FriendshipStatus status) {
        friendshipRepository.save(Friendship.builder()
                .requesterId(requesterId)
                .recipientId(recipientId)
                .status(status)
                .build());
    }

    private ReadingSession saveSession(Long hostId, Long bookId, ReadingSessionStatus status) {
        return sessionRepository.save(ReadingSession.builder()
                .hostId(hostId)
                .bookId(bookId)
                .status(status)
                .build());
    }

    private void saveParticipant(Long sessionId, Long userId) {
        participantRepository.save(SessionParticipant.builder()
                .sessionId(sessionId)
                .userId(userId)
                .build());
    }

    private SessionInvitation saveInvitation(Long sessionId, Long inviteeId, InvitationStatus status) {
        return invitationRepository.save(SessionInvitation.builder()
                .sessionId(sessionId)
                .inviteeId(inviteeId)
                .status(status)
                .build());
    }

    private void saveMessage(Long sessionId, Long senderId, String content) {
        messageRepository.save(SessionMessage.builder()
                .sessionId(sessionId)
                .senderId(senderId)
                .content(content)
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
