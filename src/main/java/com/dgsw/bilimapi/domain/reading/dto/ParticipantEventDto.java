package com.dgsw.bilimapi.domain.reading.dto;

public record ParticipantEventDto(String type, Long userId, String nickname) {

    public static ParticipantEventDto join(Long userId, String nickname) {
        return new ParticipantEventDto("JOIN", userId, nickname);
    }

    public static ParticipantEventDto leave(Long userId, String nickname) {
        return new ParticipantEventDto("LEAVE", userId, nickname);
    }

    public static ParticipantEventDto ended() {
        return new ParticipantEventDto("ENDED", null, null);
    }
}
