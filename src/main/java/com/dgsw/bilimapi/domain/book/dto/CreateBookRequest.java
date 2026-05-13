package com.dgsw.bilimapi.domain.book.dto;

import com.dgsw.bilimapi.domain.book.domain.BookCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateBookRequest(
        @NotBlank String title,
        @NotBlank String author,
        String isbn,
        String publisher,
        String description,
        BookCategory category,
        @NotNull @Min(0) Integer price,
        String contentUrl
) {
}
