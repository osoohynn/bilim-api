package com.dgsw.bilimapi.domain.reading.dto;

import com.dgsw.bilimapi.domain.reading.domain.ReadingSession;
import com.dgsw.bilimapi.domain.reading.domain.ReadingSessionStatus;
import java.time.LocalDateTime;

public record SessionResponse(
        Long id,
        Long bookId,
        String bookTitle,
        Long hostId,
        ReadingSessionStatus status,
        int participantCount,
        LocalDateTime createdAt
) {
    public static SessionResponse of(ReadingSession session, String bookTitle, int participantCount) {
        return new SessionResponse(
                session.getId(),
                session.getBookId(),
                bookTitle,
                session.getHostId(),
                session.getStatus(),
                participantCount,
                session.getCreatedAt()
        );
    }
}
