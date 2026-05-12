package com.dgsw.bilimapi.domain.friend.dto;

import java.time.LocalDateTime;

public record FriendResponse(Long friendId, String nickname, LocalDateTime lastSeenAt) {
}
