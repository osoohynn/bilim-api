package com.dgsw.bilimapi.domain.book.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateBookRequest(
        @NotBlank String title,
        @NotBlank String author,
        String isbn,
        String publisher,
        String description
) {
}
