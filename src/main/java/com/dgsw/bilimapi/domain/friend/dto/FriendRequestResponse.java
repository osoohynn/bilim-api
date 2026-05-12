package com.dgsw.bilimapi.domain.friend.dto;

import java.time.LocalDateTime;

public record FriendRequestResponse(Long requesterId, String requesterNickname, LocalDateTime requestedAt) {
}
