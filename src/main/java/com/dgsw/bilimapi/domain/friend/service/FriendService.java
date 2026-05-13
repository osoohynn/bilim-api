package com.dgsw.bilimapi.domain.friend.service;

import com.dgsw.bilimapi.commons.security.SecurityUtil;
import com.dgsw.bilimapi.domain.friend.domain.Friendship;
import com.dgsw.bilimapi.domain.friend.domain.FriendshipStatus;
import com.dgsw.bilimapi.domain.friend.dto.FriendRequestResponse;
import com.dgsw.bilimapi.domain.friend.dto.FriendResponse;
import com.dgsw.bilimapi.domain.friend.exception.AlreadyFriendsException;
import com.dgsw.bilimapi.domain.friend.exception.FriendRequestNotFoundException;
import com.dgsw.bilimapi.domain.friend.exception.FriendshipNotFoundException;
import com.dgsw.bilimapi.domain.friend.exception.SelfFriendRequestException;
import com.dgsw.bilimapi.domain.friend.repository.FriendshipRepository;
import com.dgsw.bilimapi.domain.user.domain.UserEntity;
import com.dgsw.bilimapi.domain.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    @Transactional
    public void sendRequest(Long recipientId) {
        Long requesterId = securityUtil.getCurrentUserId();
        if (requesterId.equals(recipientId)) {
            throw new SelfFriendRequestException();
        }

        boolean alreadyExists =
                friendshipRepository.existsByRequesterIdAndRecipientIdAndStatus(requesterId, recipientId, FriendshipStatus.ACCEPTED)
                || friendshipRepository.existsByRequesterIdAndRecipientIdAndStatus(recipientId, requesterId, FriendshipStatus.ACCEPTED)
                || friendshipRepository.existsByRequesterIdAndRecipientIdAndStatus(requesterId, recipientId, FriendshipStatus.PENDING)
                || friendshipRepository.existsByRequesterIdAndRecipientIdAndStatus(recipientId, requesterId, FriendshipStatus.PENDING);

        if (alreadyExists) {
            throw new AlreadyFriendsException();
        }

        friendshipRepository.save(Friendship.builder()
                .requesterId(requesterId)
                .recipientId(recipientId)
                .status(FriendshipStatus.PENDING)
                .build());
    }

    @Transactional
    public void accept(Long requesterId) {
        Long userId = securityUtil.getCurrentUserId();
        Friendship friendship = friendshipRepository
                .findByRequesterIdAndRecipientId(requesterId, userId)
                .filter(f -> f.getStatus() == FriendshipStatus.PENDING)
                .orElseThrow(FriendRequestNotFoundException::new);

        friendship.accept();
    }

    @Transactional
    public void reject(Long requesterId) {
        Long userId = securityUtil.getCurrentUserId();
        Friendship friendship = friendshipRepository
                .findByRequesterIdAndRecipientId(requesterId, userId)
                .filter(f -> f.getStatus() == FriendshipStatus.PENDING)
                .orElseThrow(FriendRequestNotFoundException::new);

        friendship.reject();
    }

    @Transactional
    public void removeFriend(Long friendId) {
        Long userId = securityUtil.getCurrentUserId();
        Friendship friendship = friendshipRepository
                .findAcceptedFriendship(userId, friendId)
                .orElseThrow(FriendshipNotFoundException::new);

        friendshipRepository.delete(friendship);
    }

    @Transactional(readOnly = true)
    public List<FriendResponse> getFriends() {
        Long userId = securityUtil.getCurrentUserId();
        return friendshipRepository.findAcceptedFriendships(userId).stream()
                .map(f -> {
                    Long friendId = f.getRequesterId().equals(userId) ? f.getRecipientId() : f.getRequesterId();
                    UserEntity friend = userRepository.findById(friendId).orElseThrow();
                    return new FriendResponse(friend.getId(), friend.getNickname(), friend.getLastSeenAt());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FriendRequestResponse> getReceivedRequests() {
        Long userId = securityUtil.getCurrentUserId();
        return friendshipRepository.findByRecipientIdAndStatus(userId, FriendshipStatus.PENDING).stream()
                .map(f -> {
                    UserEntity requester = userRepository.findById(f.getRequesterId()).orElseThrow();
                    return new FriendRequestResponse(requester.getId(), requester.getNickname(), f.getCreatedAt());
                })
                .toList();
    }
}
