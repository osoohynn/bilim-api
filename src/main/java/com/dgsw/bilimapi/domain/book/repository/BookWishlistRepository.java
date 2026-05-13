package com.dgsw.bilimapi.domain.book.repository;

import com.dgsw.bilimapi.domain.book.domain.BookWishlist;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookWishlistRepository extends JpaRepository<BookWishlist, Long> {

    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    List<BookWishlist> findByUserId(Long userId);

    Optional<BookWishlist> findByUserIdAndBookId(Long userId, Long bookId);
}
