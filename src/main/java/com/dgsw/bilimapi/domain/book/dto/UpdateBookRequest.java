package com.dgsw.bilimapi.domain.book.dto;

import com.dgsw.bilimapi.domain.book.domain.BookCategory;

public record UpdateBookRequest(
        String title,
        String author,
        String isbn,
        String publisher,
        String description,
        BookCategory category,
        Integer price,
        String contentUrl
) {
}
