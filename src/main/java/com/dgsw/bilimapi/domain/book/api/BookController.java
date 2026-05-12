package com.dgsw.bilimapi.domain.book.api;

import com.dgsw.bilimapi.domain.book.dto.BookResponse;
import com.dgsw.bilimapi.domain.book.dto.CreateBookRequest;
import com.dgsw.bilimapi.domain.book.dto.UpdateBookRequest;
import com.dgsw.bilimapi.domain.book.service.BookService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public BookResponse create(@RequestBody @Valid CreateBookRequest request) {
        return bookService.create(request);
    }

    @GetMapping
    public List<BookResponse> findAll() {
        return bookService.findAll();
    }

    @GetMapping("/{bookId}")
    public BookResponse findById(@PathVariable Long bookId) {
        return bookService.findById(bookId);
    }

    @PutMapping("/{bookId}")
    @PreAuthorize("hasRole('ADMIN')")
    public BookResponse update(@PathVariable Long bookId,
                               @RequestBody UpdateBookRequest request) {
        return bookService.update(bookId, request);
    }

    @DeleteMapping("/{bookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long bookId) {
        bookService.delete(bookId);
    }
}
