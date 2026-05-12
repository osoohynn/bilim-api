package com.dgsw.bilimapi.domain.book.dto;

import com.dgsw.bilimapi.domain.book.domain.Book;
import java.time.LocalDateTime;

public record BookResponse(
        Long id,
        String title,
        String author,
        String isbn,
        String publisher,
        String description,
        LocalDateTime createdAt
) {
    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getPublisher(),
                book.getDescription(),
                book.getCreatedAt()
        );
    }
}
