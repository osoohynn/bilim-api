package com.dgsw.bilimapi.domain.friend.api;

import com.dgsw.bilimapi.domain.friend.dto.FriendRequestResponse;
import com.dgsw.bilimapi.domain.friend.dto.FriendResponse;
import com.dgsw.bilimapi.domain.friend.service.FriendService;
import com.dgsw.bilimapi.domain.user.domain.UserEntity;
import com.dgsw.bilimapi.domain.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final UserRepository userRepository;

    @PostMapping("/request/{recipientId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void sendRequest(@PathVariable Long recipientId,
                            @AuthenticationPrincipal UserDetails userDetails) {
        friendService.sendRequest(getCurrentUserId(userDetails), recipientId);
    }

    @PostMapping("/accept/{requesterId}")
    public void accept(@PathVariable Long requesterId,
                       @AuthenticationPrincipal UserDetails userDetails) {
        friendService.accept(getCurrentUserId(userDetails), requesterId);
    }

    @PostMapping("/reject/{requesterId}")
    public void reject(@PathVariable Long requesterId,
                       @AuthenticationPrincipal UserDetails userDetails) {
        friendService.reject(getCurrentUserId(userDetails), requesterId);
    }

    @DeleteMapping("/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFriend(@PathVariable Long friendId,
                             @AuthenticationPrincipal UserDetails userDetails) {
        friendService.removeFriend(getCurrentUserId(userDetails), friendId);
    }

    @GetMapping
    public List<FriendResponse> getFriends(@AuthenticationPrincipal UserDetails userDetails) {
        return friendService.getFriends(getCurrentUserId(userDetails));
    }

    @GetMapping("/requests")
    public List<FriendRequestResponse> getRequests(@AuthenticationPrincipal UserDetails userDetails) {
        return friendService.getReceivedRequests(getCurrentUserId(userDetails));
    }

    private Long getCurrentUserId(UserDetails userDetails) {
        UserEntity user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow();
        return user.getId();
    }
}
