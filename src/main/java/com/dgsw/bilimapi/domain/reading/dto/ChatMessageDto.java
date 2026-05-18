package com.dgsw.bilimapi.domain.reading.dto;

import com.dgsw.bilimapi.domain.reading.domain.SessionMessage;
import java.time.LocalDateTime;

public record ChatMessageDto(
        Long senderId,
        String nickname,
        String content,
        LocalDateTime sentAt
) {
    public static ChatMessageDto of(SessionMessage message, String nickname) {
        return new ChatMessageDto(
                message.getSenderId(),
                nickname,
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
