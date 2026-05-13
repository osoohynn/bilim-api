package com.dgsw.bilimapi.domain.bookshelf.dto;

import com.dgsw.bilimapi.domain.book.dto.BookResponse;
import java.util.List;

public record BookshelfResponse(
        List<BookResponse> owned,
        List<BookResponse> renting,
        List<BookResponse> wishlist
) {
}
