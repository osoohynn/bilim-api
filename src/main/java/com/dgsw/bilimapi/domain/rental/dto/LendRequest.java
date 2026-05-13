package com.dgsw.bilimapi.domain.rental.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record LendRequest(
        @NotNull Long friendId,
        @NotNull Long userBookId,
        @Min(1) int days
) {
}
