package com.dgsw.bilimapi.domain.book.dto;

public record UpdateBookRequest(
        String title,
        String author,
        String isbn,
        String publisher,
        String description
) {
}
