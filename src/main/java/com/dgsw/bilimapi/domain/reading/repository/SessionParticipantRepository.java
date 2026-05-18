package com.dgsw.bilimapi.domain.reading.repository;

import com.dgsw.bilimapi.domain.reading.domain.SessionParticipant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionParticipantRepository extends JpaRepository<SessionParticipant, Long> {

    List<SessionParticipant> findBySessionId(Long sessionId);

    List<SessionParticipant> findByUserId(Long userId);

    Optional<SessionParticipant> findBySessionIdAndUserId(Long sessionId, Long userId);

    boolean existsBySessionIdAndUserId(Long sessionId, Long userId);

    int countBySessionId(Long sessionId);
}
