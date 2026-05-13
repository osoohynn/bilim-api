package com.dgsw.bilimapi.domain.rental.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AcceptRentalRequest(@NotNull @Min(1) Integer days) {
}
