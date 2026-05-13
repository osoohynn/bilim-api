package com.dgsw.bilimapi.domain.bookshelf.api;

import com.dgsw.bilimapi.domain.book.dto.BookResponse;
import com.dgsw.bilimapi.domain.book.service.BookService;
import com.dgsw.bilimapi.domain.bookshelf.dto.BookshelfResponse;
import com.dgsw.bilimapi.domain.bookshelf.dto.VisibilityRequest;
import com.dgsw.bilimapi.domain.bookshelf.service.BookshelfService;
import com.dgsw.bilimapi.domain.user.domain.UserEntity;
import com.dgsw.bilimapi.domain.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookshelf")
@RequiredArgsConstructor
public class BookshelfController {

    private final BookshelfService bookshelfService;
    private final BookService bookService;
    private final UserRepository userRepository;

    @GetMapping
    public BookshelfResponse getMyBookshelf(@AuthenticationPrincipal UserDetails userDetails) {
        return bookshelfService.getMyBookshelf(getCurrentUserId(userDetails));
    }

    @PatchMapping("/{userBookId}/visibility")
    public void setVisibility(@PathVariable Long userBookId,
                              @RequestBody VisibilityRequest request,
                              @AuthenticationPrincipal UserDetails userDetails) {
        bookshelfService.setVisibility(getCurrentUserId(userDetails), userBookId, request.isPublic());
    }

    @GetMapping("/{userId}")
    public List<BookResponse> getFriendBookshelf(@PathVariable Long userId) {
        return bookshelfService.getFriendBookshelf(userId);
    }

    @PostMapping("/wishlist/{bookId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void addWishlist(@PathVariable Long bookId,
                            @AuthenticationPrincipal UserDetails userDetails) {
        bookService.addWishlist(getCurrentUserId(userDetails), bookId);
    }

    @DeleteMapping("/wishlist/{bookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeWishlist(@PathVariable Long bookId,
                               @AuthenticationPrincipal UserDetails userDetails) {
        bookService.removeWishlist(getCurrentUserId(userDetails), bookId);
    }

    private Long getCurrentUserId(UserDetails userDetails) {
        UserEntity user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return user.getId();
    }
}
