package com.dgsw.bilimapi.domain.reading.repository;

import com.dgsw.bilimapi.domain.reading.domain.SessionMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionMessageRepository extends JpaRepository<SessionMessage, Long> {

    List<SessionMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);
}
