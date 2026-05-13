package com.dgsw.bilimapi.domain.book.repository;

import com.dgsw.bilimapi.domain.book.domain.UserBook;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBookRepository extends JpaRepository<UserBook, Long> {

    boolean existsByOwnerIdAndBookId(Long ownerId, Long bookId);

    List<UserBook> findByOwnerId(Long ownerId);

    List<UserBook> findByHolderId(Long holderId);

    Optional<UserBook> findByIdAndOwnerId(Long id, Long ownerId);

    List<UserBook> findByOwnerIdAndIsPublicTrue(Long ownerId);
}
