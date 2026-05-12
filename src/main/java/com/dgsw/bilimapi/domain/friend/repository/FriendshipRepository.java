package com.dgsw.bilimapi.domain.friend.repository;

import com.dgsw.bilimapi.domain.friend.domain.Friendship;
import com.dgsw.bilimapi.domain.friend.domain.FriendshipStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    Optional<Friendship> findByRequesterIdAndRecipientId(Long requesterId, Long recipientId);

    boolean existsByRequesterIdAndRecipientIdAndStatus(Long requesterId, Long recipientId, FriendshipStatus status);

    List<Friendship> findByRecipientIdAndStatus(Long recipientId, FriendshipStatus status);

    @Query("SELECT f FROM Friendship f WHERE (f.requesterId = :userId OR f.recipientId = :userId) AND f.status = 'ACCEPTED'")
    List<Friendship> findAcceptedFriendships(@Param("userId") Long userId);

    @Query("SELECT f FROM Friendship f WHERE ((f.requesterId = :userId AND f.recipientId = :friendId) OR (f.requesterId = :friendId AND f.recipientId = :userId)) AND f.status = 'ACCEPTED'")
    Optional<Friendship> findAcceptedFriendship(@Param("userId") Long userId, @Param("friendId") Long friendId);
}
