package com.dgsw.bilimapi.domain.reading.api;

import com.dgsw.bilimapi.domain.reading.dto.CreateSessionRequest;
import com.dgsw.bilimapi.domain.reading.dto.InvitationResponse;
import com.dgsw.bilimapi.domain.reading.dto.ChatMessageDto;
import com.dgsw.bilimapi.domain.reading.dto.SessionDetailResponse;
import com.dgsw.bilimapi.domain.reading.dto.SessionResponse;
import com.dgsw.bilimapi.domain.reading.service.ReadingSessionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reading-sessions")
@RequiredArgsConstructor
public class ReadingSessionController {

    private final ReadingSessionService service;

    @PostMapping
    public ResponseEntity<SessionDetailResponse> create(@Valid @RequestBody CreateSessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request.bookId()));
    }

    @GetMapping
    public ResponseEntity<List<SessionResponse>> getMySessions() {
        return ResponseEntity.ok(service.getMySessions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionDetailResponse> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(service.getDetail(id));
    }

    @PostMapping("/{id}/invite/{friendId}")
    public ResponseEntity<Void> invite(@PathVariable Long id, @PathVariable Long friendId) {
        service.invite(id, friendId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/invitations")
    public ResponseEntity<List<InvitationResponse>> getInvitations() {
        return ResponseEntity.ok(service.getMyInvitations());
    }

    @PostMapping("/invitations/{invitationId}/accept")
    public ResponseEntity<SessionDetailResponse> acceptInvitation(@PathVariable Long invitationId) {
        return ResponseEntity.ok(service.acceptInvitation(invitationId));
    }

    @PostMapping("/invitations/{invitationId}/reject")
    public ResponseEntity<Void> rejectInvitation(@PathVariable Long invitationId) {
        service.rejectInvitation(invitationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<Void> leave(@PathVariable Long id) {
        service.leaveSession(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> end(@PathVariable Long id) {
        service.endSession(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<ChatMessageDto>> getMessages(@PathVariable Long id) {
        return ResponseEntity.ok(service.getMessages(id));
    }

    @GetMapping("/{id}/epub")
    public ResponseEntity<Map<String, String>> getEpubUrl(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("url", service.getEpubUrl(id)));
    }
}
