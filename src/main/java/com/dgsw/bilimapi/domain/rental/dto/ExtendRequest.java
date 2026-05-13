package com.dgsw.bilimapi.domain.rental.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ExtendRequest(@NotNull @Future LocalDate newDueDate) {
}
