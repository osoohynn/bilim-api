package com.dgsw.bilimapi.domain.friend.api;

import com.dgsw.bilimapi.domain.friend.dto.FriendRequestResponse;
import com.dgsw.bilimapi.domain.friend.dto.FriendResponse;
import com.dgsw.bilimapi.domain.friend.service.FriendService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @PostMapping("/request/{recipientId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void sendRequest(@PathVariable Long recipientId) {
        friendService.sendRequest(recipientId);
    }

    @PostMapping("/accept/{requesterId}")
    public void accept(@PathVariable Long requesterId) {
        friendService.accept(requesterId);
    }

    @PostMapping("/reject/{requesterId}")
    public void reject(@PathVariable Long requesterId) {
        friendService.reject(requesterId);
    }

    @DeleteMapping("/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFriend(@PathVariable Long friendId) {
        friendService.removeFriend(friendId);
    }

    @GetMapping
    public List<FriendResponse> getFriends() {
        return friendService.getFriends();
    }

    @GetMapping("/requests")
    public List<FriendRequestResponse> getRequests() {
        return friendService.getReceivedRequests();
    }
}
