package com.dgsw.bilimapi.domain.reading.dto;

import com.dgsw.bilimapi.domain.reading.domain.ReadingSession;
import com.dgsw.bilimapi.domain.reading.domain.ReadingSessionStatus;
import java.time.LocalDateTime;
import java.util.List;

public record SessionDetailResponse(
        Long id,
        Long bookId,
        String bookTitle,
        Long hostId,
        ReadingSessionStatus status,
        String currentCfi,
        List<ParticipantInfo> participants,
        LocalDateTime createdAt
) {
    public record ParticipantInfo(Long userId, String nickname) {}

    public static SessionDetailResponse of(ReadingSession session, String bookTitle, List<ParticipantInfo> participants) {
        return new SessionDetailResponse(
                session.getId(),
                session.getBookId(),
                bookTitle,
                session.getHostId(),
                session.getStatus(),
                session.getCurrentCfi(),
                participants,
                session.getCreatedAt()
        );
    }
}
