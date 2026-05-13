package com.dgsw.bilimapi.domain.book.service;

import com.dgsw.bilimapi.commons.security.SecurityUtil;
import com.dgsw.bilimapi.domain.book.domain.Book;
import com.dgsw.bilimapi.domain.book.domain.BookCategory;
import com.dgsw.bilimapi.domain.book.domain.BookWishlist;
import com.dgsw.bilimapi.domain.book.domain.UserBook;
import com.dgsw.bilimapi.domain.book.dto.BookResponse;
import com.dgsw.bilimapi.domain.book.dto.CreateBookRequest;
import com.dgsw.bilimapi.domain.book.dto.UpdateBookRequest;
import com.dgsw.bilimapi.domain.book.exception.AlreadyPurchasedException;
import com.dgsw.bilimapi.domain.book.exception.BookNotFoundException;
import com.dgsw.bilimapi.domain.book.exception.DuplicateIsbnException;
import com.dgsw.bilimapi.domain.book.exception.NotBookOwnerException;
import com.dgsw.bilimapi.domain.book.exception.WishlistAlreadyExistsException;
import com.dgsw.bilimapi.domain.book.repository.BookRepository;
import com.dgsw.bilimapi.domain.book.repository.BookWishlistRepository;
import com.dgsw.bilimapi.domain.book.repository.UserBookRepository;
import com.dgsw.bilimapi.domain.point.service.PointService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final UserBookRepository userBookRepository;
    private final BookWishlistRepository bookWishlistRepository;
    private final PointService pointService;
    private final SecurityUtil securityUtil;

    @Transactional
    public BookResponse create(CreateBookRequest request) {
        if (request.isbn() != null && !request.isbn().isBlank()
                && bookRepository.existsByIsbn(request.isbn())) {
            throw new DuplicateIsbnException();
        }

        Book book = bookRepository.save(Book.builder()
                .title(request.title())
                .author(request.author())
                .isbn(request.isbn())
                .publisher(request.publisher())
                .description(request.description())
                .category(request.category())
                .price(request.price() != null ? request.price() : 0)
                .contentUrl(request.contentUrl())
                .build());

        return BookResponse.from(book);
    }

    @Transactional(readOnly = true)
    public List<BookResponse> search(String keyword, BookCategory category) {
        return bookRepository.search(keyword, category).stream()
                .map(BookResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookResponse findById(Long bookId) {
        return BookResponse.from(bookRepository.findById(bookId)
                .orElseThrow(BookNotFoundException::new));
    }

    @Transactional
    public BookResponse update(Long bookId, UpdateBookRequest request) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(BookNotFoundException::new);

        if (request.isbn() != null && !request.isbn().isBlank()
                && !request.isbn().equals(book.getIsbn())
                && bookRepository.existsByIsbn(request.isbn())) {
            throw new DuplicateIsbnException();
        }

        book.update(request.title(), request.author(), request.isbn(),
                request.publisher(), request.description(),
                request.category(), request.price(), request.contentUrl());

        return BookResponse.from(book);
    }

    @Transactional
    public void delete(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(BookNotFoundException::new);
        bookRepository.delete(book);
    }

    @Transactional
    public void purchase(Long bookId) {
        Long userId = securityUtil.getCurrentUserId();
        Book book = bookRepository.findById(bookId)
                .orElseThrow(BookNotFoundException::new);

        if (userBookRepository.existsByOwnerIdAndBookId(userId, bookId)) {
            throw new AlreadyPurchasedException();
        }

        pointService.deduct(userId, book.getPrice());

        userBookRepository.save(UserBook.builder()
                .bookId(bookId)
                .ownerId(userId)
                .holderId(userId)
                .isPublic(false)
                .build());
    }

    @Transactional(readOnly = true)
    public String read(Long bookId) {
        Long userId = securityUtil.getCurrentUserId();
        Book book = bookRepository.findById(bookId)
                .orElseThrow(BookNotFoundException::new);

        boolean hasAccess = userBookRepository.findByHolderId(userId).stream()
                .anyMatch(ub -> ub.getBookId().equals(bookId));

        if (!hasAccess) {
            throw new NotBookOwnerException();
        }

        return book.getContentUrl();
    }

    @Transactional
    public void addWishlist(Long bookId) {
        Long userId = securityUtil.getCurrentUserId();
        if (!bookRepository.existsById(bookId)) {
            throw new BookNotFoundException();
        }
        if (bookWishlistRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new WishlistAlreadyExistsException();
        }
        bookWishlistRepository.save(BookWishlist.builder()
                .userId(userId)
                .bookId(bookId)
                .build());
    }

    @Transactional
    public void removeWishlist(Long bookId) {
        Long userId = securityUtil.getCurrentUserId();
        BookWishlist wishlist = bookWishlistRepository.findByUserIdAndBookId(userId, bookId)
                .orElseThrow(BookNotFoundException::new);
        bookWishlistRepository.delete(wishlist);
    }
}
