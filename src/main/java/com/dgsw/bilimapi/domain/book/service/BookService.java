package com.dgsw.bilimapi.domain.book.service;

import com.dgsw.bilimapi.domain.book.domain.Book;
import com.dgsw.bilimapi.domain.book.dto.BookResponse;
import com.dgsw.bilimapi.domain.book.dto.CreateBookRequest;
import com.dgsw.bilimapi.domain.book.dto.UpdateBookRequest;
import com.dgsw.bilimapi.domain.book.exception.BookNotFoundException;
import com.dgsw.bilimapi.domain.book.exception.DuplicateIsbnException;
import com.dgsw.bilimapi.domain.book.repository.BookRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

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
                .build());

        return BookResponse.from(book);
    }

    @Transactional(readOnly = true)
    public List<BookResponse> findAll() {
        return bookRepository.findAll().stream()
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
                request.publisher(), request.description());

        return BookResponse.from(book);
    }

    @Transactional
    public void delete(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(BookNotFoundException::new);
        bookRepository.delete(book);
    }
}
