package com.dgsw.bilimapi.domain.bookshelf.service;

import com.dgsw.bilimapi.commons.security.SecurityUtil;
import com.dgsw.bilimapi.domain.book.domain.UserBook;
import com.dgsw.bilimapi.domain.book.dto.BookResponse;
import com.dgsw.bilimapi.domain.book.exception.NotBookOwnerException;
import com.dgsw.bilimapi.domain.book.repository.BookRepository;
import com.dgsw.bilimapi.domain.book.repository.BookWishlistRepository;
import com.dgsw.bilimapi.domain.book.repository.UserBookRepository;
import com.dgsw.bilimapi.domain.bookshelf.dto.BookshelfResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookshelfService {

    private final UserBookRepository userBookRepository;
    private final BookWishlistRepository bookWishlistRepository;
    private final BookRepository bookRepository;
    private final SecurityUtil securityUtil;

    @Transactional(readOnly = true)
    public BookshelfResponse getMyBookshelf() {
        Long userId = securityUtil.getCurrentUserId();
        List<UserBook> myBooks = userBookRepository.findByOwnerId(userId);

        List<BookResponse> owned = myBooks.stream()
                .filter(ub -> !ub.isRentedOut())
                .map(ub -> bookRepository.findById(ub.getBookId()).map(BookResponse::from).orElseThrow())
                .toList();

        List<BookResponse> renting = userBookRepository.findByHolderId(userId).stream()
                .filter(ub -> !ub.getOwnerId().equals(userId))
                .map(ub -> bookRepository.findById(ub.getBookId()).map(BookResponse::from).orElseThrow())
                .toList();

        List<BookResponse> wishlist = bookWishlistRepository.findByUserId(userId).stream()
                .map(w -> bookRepository.findById(w.getBookId()).map(BookResponse::from).orElseThrow())
                .toList();

        return new BookshelfResponse(owned, renting, wishlist);
    }

    @Transactional
    public void setVisibility(Long userBookId, boolean isPublic) {
        Long userId = securityUtil.getCurrentUserId();
        UserBook userBook = userBookRepository.findByIdAndOwnerId(userBookId, userId)
                .orElseThrow(NotBookOwnerException::new);
        userBook.setPublic(isPublic);
    }

    @Transactional(readOnly = true)
    public List<BookResponse> getFriendBookshelf(Long friendId) {
        return userBookRepository.findByOwnerIdAndIsPublicTrue(friendId).stream()
                .map(ub -> bookRepository.findById(ub.getBookId()).map(BookResponse::from).orElseThrow())
                .toList();
    }
}
