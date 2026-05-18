package com.dgsw.bilimapi.domain.reading.repository;

import com.dgsw.bilimapi.domain.reading.domain.InvitationStatus;
import com.dgsw.bilimapi.domain.reading.domain.SessionInvitation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionInvitationRepository extends JpaRepository<SessionInvitation, Long> {

    boolean existsBySessionIdAndInviteeId(Long sessionId, Long inviteeId);

    List<SessionInvitation> findByInviteeIdAndStatus(Long inviteeId, InvitationStatus status);
}
