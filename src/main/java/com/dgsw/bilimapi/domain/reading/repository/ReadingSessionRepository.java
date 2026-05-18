package com.dgsw.bilimapi.domain.reading.repository;

import com.dgsw.bilimapi.domain.reading.domain.ReadingSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Long> {
}
