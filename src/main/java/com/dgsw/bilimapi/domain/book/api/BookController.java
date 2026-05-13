package com.dgsw.bilimapi.domain.book.api;

import com.dgsw.bilimapi.domain.book.domain.BookCategory;
import com.dgsw.bilimapi.domain.book.dto.BookResponse;
import com.dgsw.bilimapi.domain.book.dto.CreateBookRequest;
import com.dgsw.bilimapi.domain.book.dto.UpdateBookRequest;
import com.dgsw.bilimapi.domain.book.service.BookService;
import com.dgsw.bilimapi.domain.user.domain.UserEntity;
import com.dgsw.bilimapi.domain.user.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final UserRepository userRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public BookResponse create(@RequestBody @Valid CreateBookRequest request) {
        return bookService.create(request);
    }

    @GetMapping
    public List<BookResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BookCategory category) {
        return bookService.search(keyword, category);
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

    @PostMapping("/{bookId}/purchase")
    @ResponseStatus(HttpStatus.CREATED)
    public void purchase(@PathVariable Long bookId,
                         @AuthenticationPrincipal UserDetails userDetails) {
        bookService.purchase(getCurrentUserId(userDetails), bookId);
    }

    @GetMapping("/{bookId}/read")
    public String read(@PathVariable Long bookId,
                       @AuthenticationPrincipal UserDetails userDetails) {
        return bookService.read(getCurrentUserId(userDetails), bookId);
    }

    private Long getCurrentUserId(UserDetails userDetails) {
        UserEntity user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return user.getId();
    }
}
