package com.dgsw.bilimapi.domain.reading.service;

import com.dgsw.bilimapi.commons.security.SecurityUtil;
import com.dgsw.bilimapi.domain.book.domain.Book;
import com.dgsw.bilimapi.domain.book.exception.BookNotFoundException;
import com.dgsw.bilimapi.domain.book.exception.NotBookOwnerException;
import com.dgsw.bilimapi.domain.book.repository.BookRepository;
import com.dgsw.bilimapi.domain.book.repository.UserBookRepository;
import com.dgsw.bilimapi.domain.friend.repository.FriendshipRepository;
import com.dgsw.bilimapi.domain.reading.domain.InvitationStatus;
import com.dgsw.bilimapi.domain.reading.domain.ReadingSession;
import com.dgsw.bilimapi.domain.reading.domain.ReadingSessionStatus;
import com.dgsw.bilimapi.domain.reading.domain.SessionInvitation;
import com.dgsw.bilimapi.domain.reading.domain.SessionMessage;
import com.dgsw.bilimapi.domain.reading.domain.SessionParticipant;
import com.dgsw.bilimapi.domain.reading.dto.ChatMessageDto;
import com.dgsw.bilimapi.domain.reading.dto.InvitationResponse;
import com.dgsw.bilimapi.domain.reading.dto.PageSyncDto;
import com.dgsw.bilimapi.domain.reading.dto.ParticipantEventDto;
import com.dgsw.bilimapi.domain.reading.dto.SessionDetailResponse;
import com.dgsw.bilimapi.domain.reading.dto.SessionDetailResponse.ParticipantInfo;
import com.dgsw.bilimapi.domain.reading.dto.SessionResponse;
import com.dgsw.bilimapi.domain.reading.exception.InvitationNotFoundException;
import com.dgsw.bilimapi.domain.reading.exception.NotFriendsException;
import com.dgsw.bilimapi.domain.reading.exception.NotSessionHostException;
import com.dgsw.bilimapi.domain.reading.exception.NotSessionParticipantException;
import com.dgsw.bilimapi.domain.reading.exception.SessionFullException;
import com.dgsw.bilimapi.domain.reading.exception.SessionNotFoundException;
import com.dgsw.bilimapi.domain.reading.repository.ReadingSessionRepository;
import com.dgsw.bilimapi.domain.reading.repository.SessionInvitationRepository;
import com.dgsw.bilimapi.domain.reading.repository.SessionMessageRepository;
import com.dgsw.bilimapi.domain.reading.repository.SessionParticipantRepository;
import com.dgsw.bilimapi.domain.user.domain.UserEntity;
import com.dgsw.bilimapi.domain.user.repository.UserRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReadingSessionService {

    private final ReadingSessionRepository sessionRepository;
    private final SessionParticipantRepository participantRepository;
    private final SessionInvitationRepository invitationRepository;
    private final SessionMessageRepository messageRepository;
    private final UserBookRepository userBookRepository;
    private final BookRepository bookRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public SessionDetailResponse create(Long bookId) {
        Long userId = securityUtil.getCurrentUserId();

        if (!userBookRepository.existsByOwnerIdAndBookId(userId, bookId)) {
            throw new NotBookOwnerException();
        }

        Book book = bookRepository.findById(bookId).orElseThrow(BookNotFoundException::new);

        ReadingSession session = sessionRepository.save(ReadingSession.builder()
                .hostId(userId)
                .bookId(bookId)
                .status(ReadingSessionStatus.WAITING)
                .build());

        participantRepository.save(SessionParticipant.builder()
                .sessionId(session.getId())
                .userId(userId)
                .build());

        return buildDetail(session, book);
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> getMySessions() {
        Long userId = securityUtil.getCurrentUserId();

        return participantRepository.findByUserId(userId).stream()
                .map(p -> sessionRepository.findById(p.getSessionId()).orElse(null))
                .filter(s -> s != null && s.getStatus() != ReadingSessionStatus.ENDED)
                .map(s -> {
                    Book book = bookRepository.findById(s.getBookId()).orElseThrow(BookNotFoundException::new);
                    int count = participantRepository.countBySessionId(s.getId());
                    return SessionResponse.of(s, book.getTitle(), count);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public SessionDetailResponse getDetail(Long sessionId) {
        Long userId = securityUtil.getCurrentUserId();
        ReadingSession session = findSession(sessionId);

        if (!participantRepository.existsBySessionIdAndUserId(sessionId, userId)) {
            throw new NotSessionParticipantException();
        }

        Book book = bookRepository.findById(session.getBookId()).orElseThrow(BookNotFoundException::new);
        return buildDetail(session, book);
    }

    @Transactional
    public void invite(Long sessionId, Long friendId) {
        Long userId = securityUtil.getCurrentUserId();
        ReadingSession session = findSession(sessionId);

        if (!session.getHostId().equals(userId)) {
            throw new NotSessionHostException();
        }

        friendshipRepository.findAcceptedFriendship(userId, friendId)
                .orElseThrow(NotFriendsException::new);

        if (participantRepository.countBySessionId(sessionId) >= 4) {
            throw new SessionFullException();
        }

        if (invitationRepository.existsBySessionIdAndInviteeId(sessionId, friendId)) {
            return;
        }

        invitationRepository.save(SessionInvitation.builder()
                .sessionId(sessionId)
                .inviteeId(friendId)
                .status(InvitationStatus.PENDING)
                .build());
    }

    @Transactional(readOnly = true)
    public List<InvitationResponse> getMyInvitations() {
        Long userId = securityUtil.getCurrentUserId();

        return invitationRepository.findByInviteeIdAndStatus(userId, InvitationStatus.PENDING).stream()
                .map(inv -> {
                    ReadingSession session = sessionRepository.findById(inv.getSessionId()).orElse(null);
                    if (session == null || session.getStatus() == ReadingSessionStatus.ENDED) return null;
                    Book book = bookRepository.findById(session.getBookId()).orElse(null);
                    UserEntity host = userRepository.findById(session.getHostId()).orElse(null);
                    return InvitationResponse.of(
                            inv,
                            book != null ? book.getTitle() : "",
                            host != null ? host.getNickname() : ""
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Transactional
    public SessionDetailResponse acceptInvitation(Long invitationId) {
        Long userId = securityUtil.getCurrentUserId();
        SessionInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(InvitationNotFoundException::new);

        if (!invitation.getInviteeId().equals(userId)) {
            throw new NotSessionParticipantException();
        }

        ReadingSession session = findSession(invitation.getSessionId());

        if (participantRepository.countBySessionId(session.getId()) >= 4) {
            throw new SessionFullException();
        }

        invitation.accept();

        participantRepository.save(SessionParticipant.builder()
                .sessionId(session.getId())
                .userId(userId)
                .build());

        if (session.getStatus() == ReadingSessionStatus.WAITING) {
            session.activate();
        }

        UserEntity user = securityUtil.getCurrentUser();
        messagingTemplate.convertAndSend(
                "/topic/session/" + session.getId() + "/participants",
                ParticipantEventDto.join(userId, user.getNickname())
        );

        Book book = bookRepository.findById(session.getBookId()).orElseThrow(BookNotFoundException::new);
        return buildDetail(session, book);
    }

    @Transactional
    public void rejectInvitation(Long invitationId) {
        Long userId = securityUtil.getCurrentUserId();
        SessionInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(InvitationNotFoundException::new);

        if (!invitation.getInviteeId().equals(userId)) {
            throw new NotSessionParticipantException();
        }

        invitation.reject();
    }

    @Transactional
    public void leaveSession(Long sessionId) {
        Long userId = securityUtil.getCurrentUserId();
        ReadingSession session = findSession(sessionId);

        SessionParticipant participant = participantRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(NotSessionParticipantException::new);

        participantRepository.delete(participant);

        if (session.getHostId().equals(userId)) {
            session.end();
            messagingTemplate.convertAndSend(
                    "/topic/session/" + sessionId + "/participants",
                    ParticipantEventDto.ended()
            );
        } else {
            UserEntity user = securityUtil.getCurrentUser();
            messagingTemplate.convertAndSend(
                    "/topic/session/" + sessionId + "/participants",
                    ParticipantEventDto.leave(userId, user.getNickname())
            );
        }
    }

    @Transactional
    public void endSession(Long sessionId) {
        Long userId = securityUtil.getCurrentUserId();
        ReadingSession session = findSession(sessionId);

        if (!session.getHostId().equals(userId)) {
            throw new NotSessionHostException();
        }

        session.end();
        messagingTemplate.convertAndSend(
                "/topic/session/" + sessionId + "/participants",
                ParticipantEventDto.ended()
        );
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getMessages(Long sessionId) {
        Long userId = securityUtil.getCurrentUserId();

        if (!participantRepository.existsBySessionIdAndUserId(sessionId, userId)) {
            throw new NotSessionParticipantException();
        }

        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
                .map(m -> {
                    UserEntity sender = userRepository.findById(m.getSenderId()).orElse(null);
                    return ChatMessageDto.of(m, sender != null ? sender.getNickname() : "");
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public String getEpubUrl(Long sessionId) {
        Long userId = securityUtil.getCurrentUserId();

        if (!participantRepository.existsBySessionIdAndUserId(sessionId, userId)) {
            throw new NotSessionParticipantException();
        }

        ReadingSession session = findSession(sessionId);
        return bookRepository.findById(session.getBookId())
                .orElseThrow(BookNotFoundException::new)
                .getContentUrl();
    }

    @Transactional
    public ChatMessageDto saveChat(Long sessionId, String senderEmail, String content) {
        UserEntity sender = userRepository.findByEmail(senderEmail).orElseThrow();

        if (!participantRepository.existsBySessionIdAndUserId(sessionId, sender.getId())) {
            throw new NotSessionParticipantException();
        }

        SessionMessage msg = messageRepository.save(SessionMessage.builder()
                .sessionId(sessionId)
                .senderId(sender.getId())
                .content(content)
                .build());

        return ChatMessageDto.of(msg, sender.getNickname());
    }

    @Transactional
    public PageSyncDto syncPage(Long sessionId, String hostEmail, String cfi, Double percentage) {
        UserEntity user = userRepository.findByEmail(hostEmail).orElseThrow();
        ReadingSession session = findSession(sessionId);

        if (!session.getHostId().equals(user.getId())) {
            throw new NotSessionHostException();
        }

        session.updateCfi(cfi);
        return new PageSyncDto(cfi, percentage);
    }

    private ReadingSession findSession(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(SessionNotFoundException::new);
    }

    private SessionDetailResponse buildDetail(ReadingSession session, Book book) {
        List<ParticipantInfo> participantInfos = participantRepository.findBySessionId(session.getId()).stream()
                .map(p -> {
                    UserEntity u = userRepository.findById(p.getUserId()).orElse(null);
                    return new ParticipantInfo(p.getUserId(), u != null ? u.getNickname() : "");
                })
                .toList();

        return SessionDetailResponse.of(session, book.getTitle(), participantInfos);
    }
}
