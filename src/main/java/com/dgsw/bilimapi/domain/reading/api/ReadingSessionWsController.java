package com.dgsw.bilimapi.domain.reading.api;

import com.dgsw.bilimapi.domain.reading.dto.ChatMessageDto;
import com.dgsw.bilimapi.domain.reading.dto.ChatRequest;
import com.dgsw.bilimapi.domain.reading.dto.PageSyncDto;
import com.dgsw.bilimapi.domain.reading.service.ReadingSessionService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ReadingSessionWsController {

    private final ReadingSessionService service;

    @MessageMapping("/session/{id}/chat")
    @SendTo("/topic/session/{id}/chat")
    public ChatMessageDto handleChat(
            @DestinationVariable Long id,
            @Payload ChatRequest request,
            Principal principal
    ) {
        return service.saveChat(id, principal.getName(), request.content());
    }

    @MessageMapping("/session/{id}/sync")
    @SendTo("/topic/session/{id}/sync")
    public PageSyncDto handleSync(
            @DestinationVariable Long id,
            @Payload PageSyncDto payload,
            Principal principal
    ) {
        return service.syncPage(id, principal.getName(), payload.cfi(), payload.percentage());
    }
}
