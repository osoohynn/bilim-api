package com.dgsw.bilimapi.domain.reading.dto;

import com.dgsw.bilimapi.domain.reading.domain.InvitationStatus;
import com.dgsw.bilimapi.domain.reading.domain.SessionInvitation;
import java.time.LocalDateTime;

public record InvitationResponse(
        Long id,
        Long sessionId,
        String bookTitle,
        String hostNickname,
        InvitationStatus status,
        LocalDateTime createdAt
) {
    public static InvitationResponse of(SessionInvitation invitation, String bookTitle, String hostNickname) {
        return new InvitationResponse(
                invitation.getId(),
                invitation.getSessionId(),
                bookTitle,
                hostNickname,
                invitation.getStatus(),
                invitation.getCreatedAt()
        );
    }
}
